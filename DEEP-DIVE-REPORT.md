# SAP SDK Java — Deep Dive Report

**Date:** 2026-03-05  
**Repo:** `venginetech/sap-sdk-java` (HEAD: `4d429ee`)  
**For:** Fabian Schuster, CEO vengine GmbH

---

## 1. Executive Summary

The Java SAP SDK aims to replace the existing PHP `sap-sdk` with a reusable Spring Boot library covering all SAP ByDesign middleware operations for KK. **In 24 hours of execution** (kicked off 2026-03-04), the entire production code surface has been built: 7 service clients, 35+ operations, authentication (Basic + OAuth2), response decoding, exception handling, feature-flag routing, and auto-configuration. Zero tests exist. Asana is massively out of sync — ~20 tasks show ⬜ that are actually complete in code. The real remaining work is: test suite, demo app, docker-compose, documentation, and publishing v0.1.0 to GitHub Packages. There are 6 code-level issues that need attention before this ships.

---

## 2. What Was Planned

### Scope
Full parity with the PHP SAP ByDesign SDK, packaged as a Spring Boot Starter library (`sap-sdk`) with a companion demo app (`sap-sdk-demo`).

### Architecture Decisions
| Decision | Choice |
|----------|--------|
| Build system | Maven multi-module: `sap-sdk` (library) + `sap-sdk-demo` (Spring Boot app) |
| Java version | 21 |
| URL pattern | `{baseUrl}/http/{env}/{version}/{entity}/{action}` |
| Auth | BasicAuth (default) + OAuth2 client-credentials, enum-switched |
| X-Origin-Project | AES-256-CTR + HMAC-SHA256, every request |
| Feature flags | 6 static boolean properties in `SapProperties.Features` (Flagception deferred to Phase 2) |
| Response decoding | OData `{"d": {...}}` / `{"d": {"results": [...]}}` with ArrayConverter quirk handling |
| Tests | WireMock integration tests using real SAP response shapes extracted from PHP tests |
| Rollback | PHP SDK stays live until Java SDK parity confirmed on staging |

### Execution Model
Junior (Sonnet) / Senior (Opus) subagent pair, orchestrated by Jarvis. Sequential task execution with Fabian notifications on completion.

### Risk Register (as planned)
- **RISK-001** [OPEN]: Middleware URL contract owner unknown
- **RISK-002** [ASSUMED]: X-Origin-Project encryption byte-compatibility with PHP
- **RISK-002-FLAGS** [ASSUMED]: Feature flags via static config, not Flagception
- **RISK-003** [ACCEPTED]: PHP SDK rollback plan
- **RISK-004** [ASSUMED STABLE]: Middleware URLs stable through Sep 2026
- **RISK-005** [OPEN]: 2 non-standard product routes

---

## 3. What Was Actually Built

### Code Inventory (67 Java files)

| Layer | Status | Files |
|-------|--------|-------|
| **Foundation** | ✅ 100% | `SapProperties`, `SapRoutes`, `SapAutoConfiguration`, Maven scaffold, CI pipeline |
| **Authentication** | ✅ 100% | `OAuth2BearerTokenInterceptor`, `BasicAuthInterceptor`, `SapAuthenticatedClientFactory` |
| **Core HTTP** | ✅ 100% | `BaseSapClient`, `SapResponseDecoder`, `SapExceptionHandler`, `SapResponseErrorHandler` |
| **Domain Models** | ✅ 100% | ~40 DTO classes across Account, SalesOrder, RentalOrder, Product, Employee, DeliveryCost |
| **Service Layer** | ✅ 100% | `AccountClient` (12 ops), `SalesOrderClient` (5 ops), `RentalOrderClient` (3 ops), `ProductClient` (12 ops), `RentalProductClient`, `EmployeeClient`, `DeliveryCostClient` |
| **Tests** | ❌ 0% | Zero test files. Only `.gitkeep` in test dirs. |
| **Demo App** | ❌ ~5% | Empty `main()` stub + minimal `application.yml` (no SAP config, no demo endpoints) |
| **Docker** | ❌ 0% | No `docker-compose.yml`, no WireMock stubs |
| **Documentation** | ❌ 0% | No README, no ADR, no credential guide |
| **Publishing** | ❌ 0% | CI pipeline exists but no release has been cut |

### The Asana Discrepancy

**This is a project management failure, not a code failure.** Asana shows ~20 tasks as incomplete (⬜) that are demonstrably done in code:

- All Authentication tasks (OAuth2 interceptor, ClientFactory)
- All Core HTTP Client tasks (BaseSapClient, Decoder, ExceptionHandler, observability)
- All 11 Service Layer tasks

The execution blew through tasks without updating Asana status. This means anyone looking at the Asana board (including Fabian) gets a wildly inaccurate picture of project state. **Fix this immediately.**

---

## 4. Quality Assessment

### ✅ What's Good

**Clean layering.** The `BaseSapClient → Service Client` hierarchy is well-structured. Each client gets its own `RestTemplate` via the factory (good isolation), and the decoder/exception-handler are injected (testable). The `buildUrl()` + `appendQueryParams()` pattern in `BaseSapClient` keeps URL construction consistent.

**Feature flag routing.** `SalesOrderClient.fetchRoute()` / `createRoute()` uses a clean waterfall pattern (V5 > V4 > V3 > V1). Same pattern in `ProductClient` for availability and package config. This mirrors the PHP SDK's Flagception-backed routing but with static config — a pragmatic call for Phase 1.

**SAP response handling.** `SapResponseDecoder` correctly handles both `{"d": {...}}` (single entity) and `{"d": {"results": [...]}}` (collection), plus the ArrayConverter quirk where `results` is an object instead of array. This is a real SAP quirk that would cause subtle production bugs if missed.

**Dual error handling paths.** `SapExceptionHandler` (OData `{"error": {...}}`) + `SapResponseErrorHandler` (Log/Item-based errors) cover both SAP error response formats. The PHP SDK had to handle both, and so does this.

### 🔴 Issues Found

#### Issue 1: `SapResponseErrorHandler` is dead code
**Severity: Medium.** `SapResponseErrorHandler` exists but is **never wired into any RestTemplate**. The `SapAuthenticatedClientFactory.createClient()` creates a `RestTemplate`, adds interceptors, but never calls `restTemplate.setErrorHandler(new SapResponseErrorHandler(objectMapper))`. This means the `Log.Item`-based error parsing path **does not work at all**.

Either:
- Wire it into the factory (the intended design), or
- Remove it if `SapExceptionHandler` (which catches `HttpStatusCodeException` in `BaseSapClient`) is sufficient

**My read:** You need both. `SapResponseErrorHandler` handles SAP responses that return 200 OK with error payloads in the Log envelope — `SapExceptionHandler` only triggers on HTTP 4xx/5xx. But `SapResponseErrorHandler` extends `DefaultResponseErrorHandler` which only fires on error status codes... so actually the current `SapResponseErrorHandler` *also* only fires on 4xx/5xx. **This needs architectural clarification**: does SAP middleware return errors as 200 OK + Log envelope, or only as 4xx/5xx?

#### Issue 2: Duplicated route constants — clients ignore `SapRoutes`
**Severity: High.** `SapRoutes` is a comprehensive route registry with feature-flag-aware routing methods (e.g., `getCustomerFetchRoute()` respects `customerV2EndpointEnabled`). But **no service client uses it**. Every client defines its own route string constants:

- `AccountClient` hardcodes `"v1/customer/get"` — ignores `SapRoutes.getCustomerFetchRoute()` which would return V2 when enabled
- `ProductClient` hardcodes `"v1/product/availability/get"` vs `"v2/product/availability/get"` with its own feature-flag check
- `SalesOrderClient` has its own `fetchRoute()` / `createRoute()` with inline flag logic

This means:
1. `SapRoutes` is **dead code** — it's defined as a `@Bean` in `SapAutoConfiguration` but nothing injects it
2. Route logic is **duplicated** across `SapRoutes` and individual clients
3. `AccountClient.fetch()` uses `"v1/customer/get"` always, **ignoring** the `customerV2EndpointEnabled` feature flag. This is a **bug** — when the flag is on, it should hit V2.

**Fix:** Either inject `SapRoutes` into clients and use it as the single source of route truth, or delete `SapRoutes` entirely and keep routing in clients. The current state is the worst of both worlds.

#### Issue 3: OAuth2 `synchronized` token caching — contention under load
**Severity: Medium.** The `getToken()` method is `synchronized` on the interceptor instance. Under high concurrency (50+ threads hitting SAP simultaneously), this serializes all requests through a single lock during token refresh. The token fetch itself is an HTTP call (potentially hundreds of ms), during which all other threads block.

Better pattern: use `ReentrantReadWriteLock` — readers (when token is valid) don't block each other; only the writer (token refresh) acquires exclusive access. Or use `AtomicReference<TokenHolder>` with a compare-and-set pattern.

This isn't critical for Phase 1 (KK's load won't be extreme), but it's a ticking time bomb if the SDK is used by a high-throughput service.

#### Issue 4: BasicAuth encryption — same key for AES and HMAC
**Severity: Medium-Low.** `BasicAuthInterceptor` uses the same `encryptKeyBytes` for both AES-256-CTR encryption and HMAC-SHA256 signing. Cryptographic best practice is to derive separate keys (e.g., via HKDF) from the shared secret. Using the same key for both is not catastrophically broken for AES-CTR + HMAC-SHA256, but it's a code smell that a security auditor would flag.

Also: `padOrTruncate()` zero-pads short keys, which is weak. A proper key derivation function (HKDF, PBKDF2) should be used.

**Pragmatic call:** If this must be byte-compatible with the PHP SDK's encryption (RISK-002), the implementation must match exactly — including any "wrong" practices the PHP side uses. Verify against PHP output before changing anything.

#### Issue 5: `SapAutoConfiguration` ComponentScan is too broad
**Severity: Low-Medium.** `@ComponentScan(basePackages = "com.vengine.kk.sap")` scans the entire `com.vengine.kk.sap` package tree. This will pick up **all** `@Component`/`@Service` classes — all 7 clients, the decoder, the exception handler, and the factory. If a consuming application only needs `AccountClient`, they still get all 7 clients instantiated.

This is fine for KK (which likely uses all clients), but it's not great library design. Consider:
- Conditional bean registration (`@ConditionalOnProperty`)
- Or explicit `@Bean` declarations instead of component scan

Not blocking for v0.1.0, but should be on the v0.2.0 roadmap.

#### Issue 6: `withQuery()` helper is duplicated
**Severity: Low.** Both `ProductClient` and `EmployeeClient` define their own `withQuery(String, SapQuery)` method with identical logic. Meanwhile, `AccountClient` uses `BaseSapClient.appendQueryParams()` with `query.toParamMap()`. The `appendQueryParams` approach is cleaner and already in the base class — the other two clients should use it.

---

## 5. Risk Analysis

### RISK-001 [OPEN → STILL OPEN]: Middleware URL contract owner unknown
**Assessment: Moderate risk.** Nobody has confirmed who owns the middleware URL patterns (`/http/{env}/v1/customer/get` etc.). If these change without notice, the SDK breaks silently — requests return 404 or wrong data.

**Mitigation:** Identify the owner (SAP middleware team? KK IT?) and get a written commitment that the current URL patterns are stable. This blocks production go-live.

### RISK-002 [ASSUMED → NEEDS VERIFICATION]: X-Origin-Project byte compatibility
**Assessment: Critical to verify before staging.** The Java implementation uses `padOrTruncate` for key derivation and `IV || HMAC || ciphertext` concatenation order. If the PHP SDK uses a different byte order, key derivation, or HMAC scope, the middleware will reject requests. **This must be verified with a cross-language test** — encrypt the same value with both SDKs and compare output structure.

### RISK-002-FLAGS [ASSUMED → ACCEPTABLE]: Static feature flags
**Assessment: Low risk.** Static config properties for feature flags is the right call for Phase 1. The 6 flags map cleanly to `application.yml` properties. Flagception integration (runtime flag management) is a nice-to-have for Phase 2 if KK needs per-request or per-tenant flag evaluation.

### RISK-003 [ACCEPTED → ON TRACK]: PHP SDK rollback
**Assessment: Low risk.** PHP SDK is untouched. Java SDK is in a separate repo. As long as the Java SDK is deployed alongside (not instead of) the PHP SDK during validation, rollback is trivial.

### RISK-004 [ASSUMED STABLE → ACCEPTABLE]: URL pattern stability through Sep 2026
**Assessment: Low risk.** The middleware has been running with these patterns for the PHP SDK. No evidence of planned changes. The assumption is reasonable.

### RISK-005 [OPEN → IMPLEMENTED BUT UNVERIFIED]: Non-standard product routes
**Assessment: Medium risk.** Both non-standard routes are implemented:
- `product-material-attribute/1.0.0/get` (in `ProductClient.PRODUCT_ATTRIBUTES`)
- `material-attribute/get` (in `ProductClient.MATERIAL_ATTRIBUTE`)

These work through `BaseSapClient.buildUrl()` which prepends `{baseUrl}/http/{env}/` — so the full URL becomes `{baseUrl}/http/{env}/material-attribute/get`. **Verify this is correct** — the PHP SDK may use a different base path for these routes.

### NEW RISKS IDENTIFIED

#### RISK-006 [NEW — HIGH]: SapRoutes is dead code, route duplication causes feature flag bugs
As detailed in Issue 2 above. `AccountClient` does not respect `customerV2EndpointEnabled`. If KK enables V2 customer endpoints, the Java SDK will still hit V1. **This is a functional bug.**

#### RISK-007 [NEW — MEDIUM]: SapResponseErrorHandler is not wired in
Dead code means the `Log.Item`-based error parsing path doesn't work. If SAP returns this error format on HTTP errors, clients will get generic exceptions instead of typed ones (e.g., `AccountOrderBlockException`).

#### RISK-008 [NEW — LOW]: No observability despite commit message claiming it
Git commit `f27ab23` says "structured logging + Micrometer + Sentry observability" — but there's **zero Micrometer or Sentry code** in the codebase. No `@Timed`, no `MeterRegistry`, no Sentry dependency. The only "observability" is SLF4J `log.debug/error` and MDC `sap.route` tagging in `BaseSapClient`. The commit message overpromises.

---

## 6. Gaps & Open Questions

### Genuinely Unfinished
1. **Tests** — zero tests. This is the #1 priority before any production use.
2. **WireMock fixtures** — need real SAP response shapes from PHP test data
3. **Demo app** — empty shell, no demo endpoints, no SAP config
4. **Docker compose** — doesn't exist
5. **README** — doesn't exist
6. **Publishing** — no release cut, no version tag
7. **PHP↔Java parity test** — critical for RISK-002 validation

### Architectural Decisions Still Needed
1. **SapRoutes vs client-local routing** — pick one, kill the other (see Issue 2)
2. **SapResponseErrorHandler role** — is it needed? When does SAP use Log/Item errors vs OData errors?
3. **Observability** — do we actually want Micrometer metrics? Sentry integration? If yes, it needs to be built. If no, remove the misleading commit message.
4. **RestTemplate per client** — each client creates its own `RestTemplate` via `factory.createClient()`. If using OAuth2, this means each client has its own `OAuth2BearerTokenInterceptor` with its own token cache. Is that intentional? It means 7 separate token fetches on cold start. A shared interceptor would be more efficient.
5. **SapQuery extensibility** — `SapQuery` only has `limit`, `lastId`, `countryCode`. The PHP SDK's query classes likely have more fields per domain. Are these sufficient?

---

## 7. Asana Alignment

### Tasks to Mark ✅ Done (code exists, verified)

**🔐 Authentication:**
- ⬜→✅ Implement OAuth2 Bearer Token interceptor with token caching
- ⬜→✅ Create SapAuthenticatedClientFactory (RestTemplate factory)

**🌐 Core HTTP Client:**
- ⬜→✅ Implement BaseSapClient with common HTTP operations
- ⬜→✅ Implement SapResponseDecoder for SAP envelope unwrapping
- ⬜→✅ Implement SAP exception hierarchy and SapExceptionHandler
- ⬜→✅ Add structured logging and observability to all SAP client operations *(partial — logging yes, Micrometer/Sentry no)*

**⚙️ Service Layer (all 11 tasks):**
- ⬜→✅ AccountClient — fetch, fetchByUUID, fetchById
- ⬜→✅ AccountClient — fetchByTargetGroup, checkDuplication, create, checkCreate
- ⬜→✅ AccountClient — update, deleteAddress, deleteContact, creditLimit, discounts
- ⬜→✅ SalesOrderClient (all 5 operations)
- ⬜→✅ RentalOrderClient (create, checkPrice, checkRate)
- ⬜→✅ ProductClient — fetch, fetchOne, fetchCategories, fetchAttributes
- ⬜→✅ ProductClient — pricing operations
- ⬜→✅ ProductClient — availability + stock + details + package config
- ⬜→✅ RentalProductClient (fetchSerializedItems)
- ⬜→✅ EmployeeClient (fetch)
- ⬜→✅ DeliveryCostClient (fetch shipping costs)

**🏗️ Foundation:**
- ⬜→✅ SAP-000: Project Kickoff *(code exists, onboarding doc doesn't — rescope to just mark done)*

### Tasks to Re-scope

- **"Add structured logging and observability"** — mark done for logging/MDC, create new task for Micrometer + Sentry if actually wanted
- **"Create Spring Boot Starter auto-configuration module"** — this is already done (`SapAutoConfiguration` + `.imports` file). Mark ✅.

### Total: 16 tasks need status update from ⬜ to ✅

---

## 8. Next Steps (Priority Order)

### Phase 1: Fix Critical Issues (1-2 days)
1. **Fix SapRoutes vs client routing** — inject `SapRoutes` into all clients, remove duplicate route constants. This fixes the `customerV2EndpointEnabled` bug (RISK-006).
2. **Wire `SapResponseErrorHandler` into `SapAuthenticatedClientFactory`** or remove it entirely after determining if SAP uses Log/Item error format.
3. **Update Asana** — mark 16 tasks as done. Restore trust in the project board.

### Phase 2: Test Infrastructure (3-5 days)
4. **Create WireMock test infrastructure** — `@WireMockTest` base class, server setup, JSON fixture loading.
5. **Extract WireMock fixtures from PHP tests** — get real SAP response shapes.
6. **Write unit tests** — OAuth2 interceptor (token caching, refresh, expiry), BasicAuth interceptor (encryption output verification), SapResponseDecoder (all envelope types), SapExceptionHandler (AP/SY routing).
7. **Write integration tests** — all 7 clients via WireMock, covering happy path + error paths.
8. **Cross-language encryption test** (RISK-002) — encrypt same value in PHP + Java, compare byte output.

### Phase 3: Demo & Docker (1-2 days)
9. **Build demo app** — REST endpoints that exercise each client, with WireMock as fake SAP backend.
10. **Create docker-compose.yml** — demo app + WireMock container with all 35 stub mappings.

### Phase 4: Documentation & Ship (1-2 days)
11. **Write README.md** — quick start, configuration reference, architecture overview.
12. **Write ADR** — Library vs Microservice decision record.
13. **Document credential setup** — env vars, application.yml template, encrypt key generation.
14. **Tag and publish v0.1.0** to GitHub Packages.

### Phase 5: Hardening (ongoing)
15. **Improve OAuth2 token caching** — replace `synchronized` with `ReadWriteLock` or `AtomicReference`.
16. **Add Micrometer metrics** if actually wanted — request timing, error rates per client.
17. **Add `@ConditionalOnProperty`** for selective client instantiation.
18. **Deduplicate `withQuery()` helper** — move to `BaseSapClient.appendQueryParams()` everywhere.

---

## Summary Table

| Area | Planned | Built | Gap |
|------|---------|-------|-----|
| Foundation | ✅ | ✅ | None |
| Auth | ✅ | ✅ | OAuth2 lock contention (minor) |
| Core HTTP | ✅ | ✅ | ErrorHandler not wired, SapRoutes dead code |
| Domain Models | ✅ | ✅ | None |
| Service Clients | ✅ | ✅ | Route duplication bug (Issue 2) |
| Tests | Planned | ❌ | 0% — blocking for production |
| Demo | Planned | ❌ | Empty stub only |
| Docker | Planned | ❌ | Nothing exists |
| Docs | Planned | ❌ | Nothing exists |
| Observability | Claimed | ❌ | Logging only, no metrics/Sentry |

**Bottom line:** The core library is 90% built and architecturally sound. The 10% that's missing (SapRoutes integration, ErrorHandler wiring) are real bugs that need fixing. But the real blocker is tests — this cannot ship without them. Estimated remaining effort: **8-12 days** to production-ready v0.1.0.
