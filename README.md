# SAP ByDesign Java SDK

A Spring Boot library for SAP ByDesign OData APIs — type-safe, auto-configured, production-ready.

Replaces the legacy [PHP sap-sdk](https://github.com/venginetech/sap-sdk) with a proper Java client
suitable for integration into any Spring Boot application.

**Version:** 0.1.3
**Java:** 21 | **Spring Boot:** 3.5 | **Maven**

---

## Modules

| Module | Description |
|--------|-------------|
| `sap-sdk` | The library — Spring Boot auto-configuration + all 7 SAP clients |
| `sap-sdk-demo` | Demo Spring Boot app — exercises all clients against a WireMock SAP mock |

---

## Quick Start

### 1. Add dependency

Published to GitHub Packages (`jarvis-engine/sap-sdk-java`):

```xml
<dependency>
    <groupId>com.vengine.kk</groupId>
    <artifactId>sap-sdk</artifactId>
    <version>0.1.3</version>
</dependency>
```

Add the repository to `pom.xml`:

```xml
<repositories>
  <repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/jarvis-engine/sap-sdk-java</url>
  </repository>
</repositories>
```

And credentials in `~/.m2/settings.xml`:

```xml
<servers>
  <server>
    <id>github</id>
    <username>YOUR_GITHUB_USERNAME</username>
    <password>YOUR_GITHUB_TOKEN</password>
  </server>
</servers>
```

### 2. Configure

```yaml
sap:
  base-url: https://my-sap-instance.example.com
  env: production
  origin-project: ep           # plain string — "ep" or "eshop" (optional)
  credentials:
    auth-type: BASIC
    username: sap-api-user
    password: sap-api-password
```

### 3. Use

All 7 clients are registered as Spring beans automatically. Inject directly:

```java
@RestController
public class AccountController {

    private final AccountClient accountClient;

    public AccountController(AccountClient accountClient) {
        this.accountClient = accountClient;
    }

    @GetMapping("/accounts")
    public List<Account> list() {
        return accountClient.fetch(new SapQuery().withLimit(20));
    }
}
```

---

## Configuration Reference

| Property | Description | Required |
|----------|-------------|----------|
| `sap.base-url` | SAP ByDesign base URL | ✅ |
| `sap.env` | Environment path segment (e.g. `test`, `production`) | ✅ |
| `sap.origin-project` | Origin project identifier sent as `Origin-Project` header. Values: `ep`, `eshop` | — |
| `sap.credentials.auth-type` | `BASIC` or `OAUTH2` | — (default: `BASIC`) |
| `sap.credentials.username` | Basic auth username | BASIC only |
| `sap.credentials.password` | Basic auth password | BASIC only |
| `sap.credentials.oauth-client-id` | OAuth2 client ID | OAUTH2 only |
| `sap.credentials.oauth-client-secret` | OAuth2 client secret | OAUTH2 only |
| `sap.credentials.oauth-token-url` | OAuth2 token endpoint URL | OAUTH2 only |
| `sap.features.customer-v2-endpoint-enabled` | Use V2 customer endpoints | — (default: false) |
| `sap.features.sales-order-v3-endpoint-enabled` | Use V3 sales order endpoints | — (default: false) |
| `sap.features.sales-order-v4-endpoint-enabled` | Use V4 sales order endpoints | — (default: false) |
| `sap.features.sales-order-v5-endpoint-enabled` | Use V5 (highest priority) | — (default: false) |
| `sap.features.product-availability-v2-endpoint-enabled` | Use V2 availability endpoint | — (default: false) |
| `sap.features.package-configuration-v2-endpoint-enabled` | Use V2 package config endpoint | — (default: false) |

The auto-configuration is a no-op if `sap.base-url` is not set — safe to include in the classpath without configuring.

---

## Clients

All clients are registered as Spring beans via `SapAutoConfiguration`. Each can be overridden with `@ConditionalOnMissingBean`.

| Client | Operations |
|--------|------------|
| `AccountClient` | `fetch`, `fetchByUUID`, `fetchById`, `fetchByIds`, `fetchByTargetGroup`, `checkDuplication`, `create`, `update`, `deleteAddress`, `createAddress`, `deleteContact`, `createContact` |
| `ProductClient` | `fetch`, `fetchOne`, `fetchCategories`, `fetchAttributes`, `fetchPackageConfigurations`, `fetchSalesPriceLists`, `fetchPrices`, `fetchPricesByPriceList`, `fetchAvailability`, `fetchStock`, `fetchDetails`, `fetchMaterialAttribute` |
| `SalesOrderClient` | `fetch`, `fetchOne`, `create`, `update`, `cancel` |
| `RentalOrderClient` | `create`, `checkPrice`, `checkRate` |
| `RentalProductClient` | `fetchSerializedItems` |
| `EmployeeClient` | `fetch` |
| `DeliveryCostClient` | `fetch` |

---

## Feature Flags

Feature flags route requests to newer SAP API versions without code changes. Evaluated at request time from `application.yml` — restart required to change.

**Sales Order routing** (V5 > V4 > V3 > V1 default):
```yaml
sap.features.sales-order-v5-endpoint-enabled: true   # uses V5, overrides V3/V4
```

**Customer routing** (V2 when enabled, V1 default):
```yaml
sap.features.customer-v2-endpoint-enabled: true
```

---

## Authentication

### BASIC (default)

```yaml
sap:
  credentials:
    auth-type: BASIC
    username: sap-api-user
    password: sap-api-password
  origin-project: ep
```

Sends `Authorization: Basic <base64>` + `Origin-Project: ep` on every request.

### OAUTH2

```yaml
sap:
  credentials:
    auth-type: OAUTH2
    oauth-client-id: your-client-id
    oauth-client-secret: your-client-secret
    oauth-token-url: https://your-sap.example.com/oauth/token
  origin-project: ep
```

Fetches a `client_credentials` token automatically. Token is cached and refreshed 30 seconds before expiry. Also sends `Origin-Project: ep` on every request.

---

## Error Handling

The SDK throws two exception types — both are unchecked (`RuntimeException`):

| Exception | When thrown | Log level |
|-----------|-------------|-----------|
| `AccountOrderBlockException` | SAP returns AP* error code or order-block TypeID | `WARN` |
| `SapClientException` | All other SAP errors, timeouts, deserialization failures | `WARN` (business) / `ERROR` (system/unexpected) |

`AccountOrderBlockException` extends `SapClientException` — catching `SapClientException` catches both.

Example handler:

```java
@RestControllerAdvice
public class SapErrorHandler {

    @ExceptionHandler(AccountOrderBlockException.class)
    public ResponseEntity<?> orderBlock(AccountOrderBlockException e) {
        return ResponseEntity.unprocessableEntity()
                .body(Map.of("error", "account_blocked", "message", e.getMessage()));
    }

    @ExceptionHandler(SapClientException.class)
    public ResponseEntity<?> sapError(SapClientException e) {
        return ResponseEntity.status(502)
                .body(Map.of("error", "sap_error", "message", e.getMessage()));
    }
}
```

---

## Running the Demo

The demo app simulates all SAP responses via WireMock — no real SAP credentials needed.

### Start

```bash
docker compose up --build
```

Starts:
- **WireMock** on `localhost:9090` — simulates SAP ByDesign
- **Demo app** on `localhost:8080` — Spring Boot app using the SDK

### Endpoints

```bash
# Accounts
curl http://localhost:8080/demo/accounts
curl http://localhost:8080/demo/accounts/acc-uuid-001
curl "http://localhost:8080/demo/accounts/check-duplicate?name=Test&street=Main%20St&city=Berlin&country=DE"

# Products
curl http://localhost:8080/demo/products
curl http://localhost:8080/demo/products/P001
curl "http://localhost:8080/demo/products/P001/availability?from=2026-03-01&to=2026-03-31"
curl http://localhost:8080/demo/products/categories
curl http://localhost:8080/demo/products/price-lists

# Sales orders
curl http://localhost:8080/demo/sales-orders
curl http://localhost:8080/demo/sales-orders/SO-100001

# Rental
curl http://localhost:8080/demo/rental/items

# Employees
curl http://localhost:8080/demo/employees

# Health
curl http://localhost:8080/actuator/health
```

---

## Development

### Build

```bash
mvn clean package
```

### Test

```bash
mvn test
```

Tests use [WireMock Spring Boot](https://github.com/maciejwalkowiak/wiremock-spring-boot) — no external services needed.

### Run demo without Docker

```bash
# Terminal 1 — start WireMock standalone
docker run -p 9090:8080 -v $(pwd)/wiremock:/home/wiremock wiremock/wiremock:3.3.1

# Terminal 2 — start demo
SAP_BASE_URL=http://localhost:9090 SAP_ENV=test mvn spring-boot:run -pl sap-sdk-demo
```

---

## Risk Register

| ID | Status | Risk | Notes |
|----|--------|------|-------|
| **RISK-001** | 🟡 Open | URL contract: SDK builds URLs as `{base-url}/http/{env}/{route}`. SAP-side restructuring breaks all clients without code change. | Routes are constants in each client. `BaseSapClient.buildUrl()` is the single mutation point. |
| **RISK-002** | ✅ Resolved | Origin-Project header: Previously implemented with AES-256-CTR + HMAC-SHA256 encryption. PHP SDK actually sends it as a plain string (`ep`, `eshop`). Fixed in v0.1.2. | Both header name (`Origin-Project`, not `X-Origin-Project`) and value format are now correct. |
| **RISK-005** | 🟡 Open | Non-standard routes: Two product endpoints don't follow the `v{N}/resource/action` pattern (`product-material-attribute/1.0.0/get`, `material-attribute/get`). | Documented in `ProductClient`. `buildUrl()` handles them correctly. Monitor if SAP renames these. |

---

## License

Private — vengine GmbH. Not for redistribution.
