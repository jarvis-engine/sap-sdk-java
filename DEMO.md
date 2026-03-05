# SAP ByDesign Java SDK — Demo Guide

> **Audience:** Jan + Blubito `rental-service` team  
> **TL;DR:** Drop one Maven dependency into your Spring Boot app. Get type-safe SAP clients. Never write HTTP/auth/error-handling boilerplate again.

---

## 1. Start the demo (30 seconds)

```bash
git clone https://github.com/jarvis-engine/sap-sdk-java
cd sap-sdk-java
docker compose up
```

Open **http://localhost:8080/swagger-ui.html**

WireMock runs on port 9090 with pre-loaded Kurt König data (Liebherr excavators, cranes, rental orders). The demo app connects to it automatically — no SAP instance needed.

---

## 2. The rental flow (live demo)

Follow these steps in Swagger UI:

### Step 1 — List available machines

```
GET /demo/rental/items?limit=8
```

Returns 8 physical machines tracked by serial number:

```json
[
  { "internalUuid": "f4a1-0002", "articleNumber": "MAT-20002",
    "serialNumber": "SN-LH-R936-001", "costCenterId": "CC-BAGGER-01", "enabled": true },
  { "internalUuid": "f4a1-0004", "articleNumber": "MAT-20003",
    "serialNumber": "SN-LH-LTM-001",  "costCenterId": "CC-KRAN-01",   "enabled": true },
  { "internalUuid": "f4a1-0008", "articleNumber": "MAT-20007",
    "serialNumber": "SN-LH-LTR-001",  "costCenterId": "CC-KRAN-01",   "enabled": false }
]
```

`enabled: false` = currently unavailable (maintenance, rented out, etc.)

---

### Step 2 — Check rental price

```
POST /demo/rental/price-check
```

Body:
```json
{
  "salesUnitId": "SU-DE-01",
  "distributionChannel": "01",
  "products": [{
    "internalId": "MAT-20002",
    "quantity": 1,
    "startDateTime": "2026-04-01T08:00:00Z",
    "endDateTime": "2026-04-30T17:00:00Z"
  }]
}
```

Returns the calculated price for the Liebherr R936 over April: **€4,200.00**

---

### Step 3 — Check rental rate

```
POST /demo/rental/rate-check
```

Body:
```json
{
  "companyId": "KK-DE",
  "quantity": 1,
  "calculationMode": "1"
}
```

---

### Step 4 — Create rental order

```
POST /demo/rental/orders
```

Body:
```json
{
  "accountId": "1000001",
  "serialNumber": "SN-LH-R936-001",
  "startDate": "2026-04-01",
  "endDate": "2026-04-30"
}
```

SAP creates the order and returns the ID:
```json
{ "internalId": "RO-2026-0042", "uuid": "a3bc-9921" }
```

---

### Step 5 — Error handling (no try/catch needed)

```
GET /demo/rental/items/error-demo
```

When SAP returns an error (unknown item, order block, system error), the SDK automatically:
1. Parses the SAP OData error payload
2. Throws the appropriate exception (`SapClientException` or `AccountOrderBlockException`)
3. Spring's `@ControllerAdvice` maps it to a clean HTTP response

Response:
```json
HTTP 502
{
  "error": "SAP error",
  "message": "Serialized item with serialNumber 'SN-LH-R920-UNKNOWN' does not exist in SAP ByDesign."
}
```

**Your service code stays clean. No SAP error parsing, no HTTP status checking.**

---

## 3. Drop it into rental-service (5 minutes)

### Step 1 — Add the dependency

```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.vengine.kk</groupId>
    <artifactId>sap-sdk</artifactId>
    <version>0.1.2</version>
</dependency>

<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/jarvis-engine/sap-sdk-java</url>
    </repository>
</repositories>
```

GitHub Packages credentials in `~/.m2/settings.xml`:
```xml
<servers>
    <server>
        <id>github</id>
        <username>YOUR_GITHUB_USERNAME</username>
        <password>YOUR_GITHUB_TOKEN</password>
    </server>
</servers>
```

### Step 2 — Configure

```yaml
# application.yml
sap:
  base-url: https://your-instance.bydesign.cloud.sap
  env: prod
  credentials:
    auth-type: BASIC
    username: ${SAP_USER}
    password: ${SAP_PASS}
```

### Step 3 — Use the clients

```java
@Service
public class RentalService {

    private final RentalProductClient rentalProductClient;
    private final RentalOrderClient rentalOrderClient;

    // Spring auto-wires both clients — no @Bean setup needed
    public RentalService(RentalProductClient rentalProductClient,
                         RentalOrderClient rentalOrderClient) {
        this.rentalProductClient = rentalProductClient;
        this.rentalOrderClient = rentalOrderClient;
    }

    public List<SerializedItem> getAvailableMachines() {
        SapQuery query = new SapQuery();
        query.setLimit("50");
        return rentalProductClient.fetchSerializedItems(query);
    }

    public RentalOrder bookMachine(Map<String, Object> orderRequest) {
        return rentalOrderClient.create(orderRequest);
        // Throws SapClientException automatically if SAP returns an error
    }
}
```

That's it. No `RestTemplate` setup, no OAuth2 config, no error parsing. **The SDK handles all of that.**

---

## 4. Develop without SAP access

The WireMock stubs ship with the SDK. Point your local `rental-service` at WireMock instead of SAP:

```yaml
# application-local.yml
sap:
  base-url: http://localhost:9090
  env: test
  credentials:
    auth-type: BASIC
    username: demo-user
    password: demo-pass
```

```bash
# Start WireMock standalone
docker run -p 9090:8080 \
  -v $(pwd)/wiremock:/home/wiremock \
  wiremock/wiremock:3.3.1
```

Your entire team can develop and test rental flows without touching the SAP ByDesign instance. Parallel development, no SAP conflicts, no test data pollution.

---

## 5. What's included

| Client | Operations |
|--------|------------|
| `RentalProductClient` | `fetchSerializedItems()` — machines with serial numbers |
| `RentalOrderClient` | `create()`, `checkPrice()`, `checkRate()` |
| `SalesOrderClient` | `fetch()`, `fetchOne()`, `create()`, `cancel()` |
| `ProductClient` | `fetch()`, `fetchOne()`, `fetchCategories()`, `fetchAvailability()`, `fetchPrices()` |
| `AccountClient` | `fetch()`, `fetchByUUID()`, `checkDuplication()` |
| `InvoiceClient` | `fetch()`, `fetchOne()` |
| `DeliveryCostClient` | `fetch()` |

All clients are Spring Boot auto-configured. Add the dependency, add the config — they're ready to `@Autowired`.

---

## 6. Architecture

```
rental-service (your app)
    │
    ├── @Autowired RentalProductClient
    ├── @Autowired RentalOrderClient
    │
    └── sap-sdk (library)
            ├── Auth (BASIC / OAuth2 — configured via application.yml)
            ├── SapResponseErrorHandler (parses OData errors automatically)
            ├── SapResponseDecoder (unwraps d.results / d envelope)
            └── BaseSapClient (HTTP, logging, MDC tracing)
```

---

## Questions?

Contact: **Fabian Schuster** · fabian@vengine.tech · vengine GmbH
