# SAP ByDesign Java SDK

Spring Boot library for SAP ByDesign OData APIs. Replaces the legacy [PHP sap-sdk](https://github.com/jarvis-engine/sap-sdk) with a type-safe, auto-configured Java client.

**Modules:**

- `sap-sdk` — the library (Spring Boot starter with auto-configuration)
- `sap-sdk-demo` — demo Spring Boot app exercising all SDK clients

## Quick Start

### 1. Add Maven dependency

The SDK is published to GitHub Packages:

```xml
<dependency>
    <groupId>com.vengine.kk</groupId>
    <artifactId>sap-sdk</artifactId>
    <version>0.1.0</version>
</dependency>
```

Configure GitHub Packages authentication in your `~/.m2/settings.xml`:

```xml
<servers>
  <server>
    <id>github</id>
    <username>YOUR_GITHUB_USERNAME</username>
    <password>YOUR_GITHUB_TOKEN</password>
  </server>
</servers>
```

And add the repository to your `pom.xml`:

```xml
<repositories>
  <repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/jarvis-engine/sap-sdk-java</url>
  </repository>
</repositories>
```

### 2. Configure `application.yml`

```yaml
sap:
  base-url: https://your-sap-instance.sapbydesign.com
  env: production
  credentials:
    auth-type: BASIC
    username: your-user
    password: your-password
  features:
    customer-v2-endpoint-enabled: false
    sales-order-v3-endpoint-enabled: false
```

### 3. Inject and use clients

```java
@RestController
public class MyController {

    private final AccountClient accountClient;

    public MyController(AccountClient accountClient) {
        this.accountClient = accountClient;
    }

    @GetMapping("/accounts")
    public List<Account> getAccounts() {
        SapQuery query = new SapQuery();
        query.setLimit("10");
        return accountClient.fetch(query);
    }
}
```

All clients are auto-configured as Spring beans — just inject them.

## Configuration Reference

All properties are under the `sap` prefix:

| Property | Description | Default |
|----------|-------------|---------|
| `sap.base-url` | SAP ByDesign base URL | *required* |
| `sap.env` | Environment segment in URL path (e.g. `test`, `production`) | *required* |
| `sap.encrypt-key` | Encryption key for sensitive payloads | — |
| `sap.origin-project` | Origin project identifier | — |
| `sap.credentials.auth-type` | Authentication type: `BASIC` or `OAUTH2` | `BASIC` |
| `sap.credentials.username` | Basic auth username | — |
| `sap.credentials.password` | Basic auth password | — |
| `sap.credentials.oauth-client-id` | OAuth2 client ID | — |
| `sap.credentials.oauth-client-secret` | OAuth2 client secret | — |
| `sap.credentials.oauth-token-url` | OAuth2 token endpoint URL | — |
| `sap.features.customer-v2-endpoint-enabled` | Use V2 customer fetch/create/update endpoints | `false` |
| `sap.features.sales-order-v3-endpoint-enabled` | Use V3 sales order endpoints | `false` |
| `sap.features.sales-order-v4-endpoint-enabled` | Use V4 sales order endpoints | `false` |
| `sap.features.sales-order-v5-endpoint-enabled` | Use V5 sales order endpoints | `false` |
| `sap.features.product-availability-v2-endpoint-enabled` | Use V2 product availability endpoint | `false` |
| `sap.features.package-configuration-v2-endpoint-enabled` | Use V2 package configuration endpoint | `false` |

## Clients Overview

| Client | Bean Type | Key Operations |
|--------|-----------|----------------|
| **AccountClient** | `@Service` | `fetch`, `fetchByUUID`, `fetchById`, `checkDuplication`, `create`, `update`, `deleteAddress`, `createAddress` |
| **ProductClient** | `@Component` | `fetch`, `fetchOne`, `fetchCategories`, `fetchAvailability`, `fetchSalesPriceLists`, `fetchPrices` |
| **SalesOrderClient** | `@Component` | `fetch`, `fetchOne`, `create`, `update`, `cancel` |
| **RentalOrderClient** | `@Component` | `create`, `priceCheck`, `rateCheck` |
| **RentalProductClient** | `@Component` | `fetchSerializedItems` |
| **EmployeeClient** | `@Component` | `fetch` |
| **DeliveryCostClient** | `@Component` | `fetch` |

## Feature Flags

Feature flags control which API version is used for specific endpoints. They allow gradual migration to newer SAP ByDesign API versions:

| Flag | Effect |
|------|--------|
| `customer-v2-endpoint-enabled` | Routes `AccountClient.fetch()` and `create/update` to V2 endpoints |
| `sales-order-v3-endpoint-enabled` | Routes `SalesOrderClient.fetch()` and `create()` to V3 |
| `sales-order-v4-endpoint-enabled` | Overrides V3 → routes to V4 |
| `sales-order-v5-endpoint-enabled` | Overrides V4 → routes to V5 (highest priority) |
| `product-availability-v2-endpoint-enabled` | Routes `ProductClient.fetchAvailability()` to V2 |
| `package-configuration-v2-endpoint-enabled` | Routes `ProductClient.fetchPackageConfigurations()` to V2 |

Sales order flags are evaluated in priority order: V5 > V4 > V3 > V1 (default).

## Authentication

### BASIC (default)

```yaml
sap:
  credentials:
    auth-type: BASIC
    username: sap-api-user
    password: sap-api-password
```

### OAUTH2

```yaml
sap:
  credentials:
    auth-type: OAUTH2
    oauth-client-id: your-client-id
    oauth-client-secret: your-client-secret
    oauth-token-url: https://your-sap-instance.sapbydesign.com/sap/bc/sec/oauth2/token
```

The SDK automatically handles token acquisition and adds Bearer tokens to requests via `OAuth2BearerTokenInterceptor`.

## Error Handling

The SDK provides a structured exception hierarchy:

- **`SapClientException`** — base exception for all SAP API errors (HTTP errors, timeouts, deserialization failures)
- **`AccountOrderBlockException`** extends `SapClientException` — thrown when an account has an active order block

Both are unchecked (`RuntimeException`). Example handling:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountOrderBlockException.class)
    public ResponseEntity<?> handleBlocked(AccountOrderBlockException ex) {
        return ResponseEntity.status(422)
            .body(Map.of("error", "Account blocked", "message", ex.getMessage()));
    }

    @ExceptionHandler(SapClientException.class)
    public ResponseEntity<?> handleSapError(SapClientException ex) {
        return ResponseEntity.status(502)
            .body(Map.of("error", "SAP error", "message", ex.getMessage()));
    }
}
```

## Running the Demo

The demo app uses WireMock to simulate SAP ByDesign responses — no real SAP credentials needed.

### With Docker Compose

```bash
docker compose up --build
```

This starts:
- **WireMock** on port 9090 (SAP mock)
- **Demo app** on port 8080

### Try the endpoints

```bash
# List accounts
curl http://localhost:8080/demo/accounts

# Get single account
curl http://localhost:8080/demo/accounts/acc-uuid-001

# Check for duplicates
curl "http://localhost:8080/demo/accounts/check-duplicate?name=Test&street=Main%20St&city=Berlin&country=DE"

# List products
curl http://localhost:8080/demo/products

# Get product availability
curl "http://localhost:8080/demo/products/P001/availability?from=2026-03-01&to=2026-03-31"

# Product categories
curl http://localhost:8080/demo/products/categories

# Price lists
curl http://localhost:8080/demo/products/price-lists

# Sales orders
curl http://localhost:8080/demo/sales-orders

# Single sales order
curl http://localhost:8080/demo/sales-orders/SO-100001

# Rental items
curl http://localhost:8080/demo/rental/items

# Employees
curl http://localhost:8080/demo/employees

# Health check
curl http://localhost:8080/actuator/health
```

## Development

### Prerequisites

- Java 21
- Maven 3.9+

### Build

```bash
mvn clean package
```

### Run tests

```bash
mvn test
```

Tests use [WireMock Spring Boot](https://github.com/maciejwalkowiak/wiremock-spring-boot) for integration testing against mocked SAP responses.

### Run demo locally (without Docker)

```bash
# Start WireMock standalone first, then:
SAP_BASE_URL=http://localhost:9090 SAP_ENV=test \
  mvn spring-boot:run -pl sap-sdk-demo
```

## Risk Register

| ID | Risk | Mitigation |
|----|------|------------|
| **RISK-001** | URL contract: SDK builds URLs as `{baseUrl}/http/{env}/{route}`. Any SAP-side URL restructuring breaks all clients. | Routes are defined as constants in each client class. Central `BaseSapClient.buildUrl()` makes changes single-point. |
| **RISK-002** | AES byte-compatibility: The `X-Origin-Project` header uses AES-256-CTR + HMAC-SHA256. Wire format: `Base64(IV[16] \|\| HMAC-SHA256(IV\|\|ciphertext)[32] \|\| ciphertext)`. If the Java output differs from the PHP SDK byte-for-byte, every SAP request is rejected by the middleware. Cross-language verification is **pending** before staging go-live. | Verify by encrypting identical plaintext in both SDKs and comparing base64 output byte-by-byte. |
| **RISK-005** | Non-standard routes: Some SAP endpoints (e.g. `material-attribute/get`) don't follow the `v{N}/resource/action` pattern. | These routes are explicitly documented in `ProductClient` and work correctly with `buildUrl()`. |

## License

Private — vengine GmbH. Not for redistribution.
