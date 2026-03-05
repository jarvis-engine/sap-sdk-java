# PHP SAP SDK — Complete Spec for Java Migration
> Source: venginetech/sap-sdk v31.24.6 (TeleSoftas)
> Generated: 2026-03-05 by Jarvis — used as authoritative spec for sap-sdk-java

---

## 0. Architecture Overview

### Response envelope (CRITICAL — Java SDK currently wrong)

SAP ByDesign does NOT return standard OData `{"d": {"results": []}}`.
It returns complex XML-to-JSON wrapped responses with operation-name root keys.

**PHP `ResponseContentsDecoder.decode()` algorithm:**

```
1. Parse JSON → get first key = $operation
2. If $operation contains "_sync"
   OR contains "Collection"
   OR contains "KKDigital_GetEmployees"
   OR equals "n0:SalesOrderByElementsResponse_sync"
   OR is in IGNORED_COLLECTIONS
   → unwrap: $contents = $payload[$operation]
   else: $contents = $payload (already unwrapped, e.g. OData "d" format)
3. Check for errors in $contents
4. Return $contents (caller accesses $contents[$nodeKey])
```

**IGNORED_COLLECTIONS** (also get unwrapped):
- `KKDigital_GetCustomers_DirectSales`
- `KKDigital_GetCustomers_OnlineSales`
- `KKDigital_GetCustomers`
- `SalesOrderHistory`
- `SalesOrdersShippingConditions`
- `n0:ServiceOrderByElementsResponse_synC` ← rental order create response
- `n0:RentalRateCalculatorReadByIDResponse_sync` ← rental rate check response

### Error handling

**PHP `SapExceptionHandler.handle()`** — checks BEFORE response parsing:
- Checks for `ObjectLockedException` if Log.Item contains TypeID `101(/AP_ESI_COMMON/)` and Note contains "Locking"
- Checks root nodes: `n0:CustomerBundleMaintainConfirmation_sync_V1`, `n0:SalesOrderBundleMaintainConfirmation_sync`, `n0:ServiceOrderByElementsResponse_synC`, etc.
- Maps 40+ TypeID codes to specific exceptions

**Exception types** (Java SDK only implements 2 of these):
| PHP Exception | Java Equivalent | Trigger |
|---|---|---|
| `SapClientException` | `SapClientException` ✅ | General / unidentified |
| `AccountOrderBlockException` | `AccountOrderBlockException` ✅ | TypeID `034(/CL_CDA_BUSDT/)` |
| `AccountException` | ❌ missing | Customer/account errors |
| `SalesOrderException` | ❌ missing | Order processing errors |
| `ProductException` | ❌ missing | Catalog errors |
| `PriceListException` | ❌ missing | Price list errors |
| `ObjectLockedException` | ❌ missing | SAP object locked |

### URL pattern

```
{baseUrl}/http/{env}/{route}
```
Routes class builds: `sprintf('%s/http/%s%s', baseUrl, env, route)`

### Feature flags (PHP → Java mapping)

| PHP Feature Constant | Java `sap.features.*` | Default |
|---|---|---|
| `SALES_ORDER_V3_ENDPOINT_ENABLED` | `sales-order-v3-endpoint-enabled` | false |
| `SALES_ORDER_V4_ENDPOINT_ENABLED` | `sales-order-v4-endpoint-enabled` | false |
| `SALES_ORDER_V5_ENDPOINT_ENABLED` | `sales-order-v5-endpoint-enabled` | false |
| `CUSTOMER_V2_ENDPOINT_ENABLED` | `customer-v2-endpoint-enabled` | false |
| `PACKAGE_CONFIGURATION_V2_ENDPOINT_ENABLED` | `package-configuration-v2-endpoint-enabled` | false |
| `PRODUCT_AVAILABILITY_V2_ENDPOINT_ENABLED` | `product-availability-v2-endpoint-enabled` | false |

---

## 1. RentalProductClient

### Method: `fetchSerializedItems()`

**Route:** `GET /v1/service-product/get`

**SAP Response envelope:**
```
$operation = first key of response (some "_sync" key or "FixedAsset" directly)
$nodeKey = "FixedAsset"
$contents["FixedAsset"] = array of items
```

**SAP field → Java field mapping (SerializedItemNormalizer):**
| SAP Field | Java Field | Notes |
|---|---|---|
| `UUID` | `internalUuid` | |
| `VVS_UnitType` | `articleNumber` | |
| `VVS_UnitNumber` | `serialNumber` | |
| `CostCentreID` | `costCenterId` | |
| `VVS_EnabledForFieldVu` | `enabled` | String `"true"` → boolean true; absent/other → false |

**Validation (SerializedItemValidator) — item is SKIPPED if:**
- `VVS_UnitType` not set
- `VVS_UnitNumber` not set
- `CostCentreID` not set

**Java SDK gaps:**
- ❌ WireMock stubs use Java field names (`internalUuid`, `articleNumber`) — real SAP sends `UUID`, `VVS_UnitType` etc.
- ❌ No item validation (Java returns all items including invalid ones)
- ❌ Response decoder uses `d.results` — wrong format

---

## 2. RentalOrderClient

### Method: `create(RentalOrderInput input): RentalOrder`

**Route:** `POST /v1/service-order/post`

**SAP Request payload (from RentalOrderNormalizer.normalize()):**
```json
{
  "uuid": "...",
  "deliveryBranch": "...",
  "sourceSystem": "...",
  "startDateTime": "2026-04-01T08:00:00+00:00",
  "endDateTime": "2026-04-30T17:00:00+00:00",
  "originCreationDate": "2026-03-05T00:00:00+00:00",
  "serviceOrderEnabledForFieldVu": true,
  "name": "...",
  "buyerId": "...",
  "currency": "EUR",
  "customer": { "internalId": "1000001" },
  "accountParty": { "internalId": "...", "name": "..." },
  "billToParty": { "internalId": "...", "name": "..." },
  "productRecipientParty": { "internalId": "...", "name": "..." },
  "employeeResponsibleParty": { "internalId": "EMP-001" },
  "sellerParty": { "internalId": "KK-DE" },
  "fieldVuDeliveryBranch": null,
  "serviceExecutionParty": { "internalId": null },
  "salesUnitParty": { "internalId": "SU-DE-01" },
  "release": false,
  "products": [
    {
      "internalId": "...",
      "quantity": 1,
      "startDateTime": "2026-04-01T08:00:00+00:00",
      "endDateTime": "2026-04-30T17:00:00+00:00",
      "discount": "0",
      "productPrice": "4200.00"
    }
  ]
}
```

**SAP Response envelope:**
- Operation: `n0:ServiceOrderByElementsResponse_synC` (in IGNORED_COLLECTIONS → unwrapped)
- nodeKey: `ServiceOrder`
- Access: `$contents["ServiceOrder"]`

**Output fields (from RentalOrderNormalizer.denormalize()):**
```
$data["ServiceOrder"]["ID"] → internalId
$data["ServiceOrder"]["UUID"] → uuid
```

**Java SDK gaps:**
- ❌ Takes `Map<String,Object>` instead of typed `RentalOrderInput`
- ❌ Response decoder can't handle `n0:ServiceOrderByElementsResponse_synC` envelope
- ❌ Input DTO has zero type safety; caller must know exact field names manually

---

### Method: `checkPrice(RentalPriceCheckInput input): List<RentalPriceCheckProduct>`

**Route:** `POST /v1/service-order/rental-price/get`

**SAP Request payload (from RentalPriceCheckNormalizer.normalize()):**
```json
{
  "application": "2",
  "currencyCode": "EUR",
  "pricingDate": "2026-04-01",
  "accountId": "1000001",
  "salesOrganisationId": "SU-DE-01",
  "distributionChannel": "01",
  "companyId": "KK-DE",
  "pricingModel": null,
  "items": [
    {
      "productId": "MAT-20002",
      "productTypeCode": "MATERIAL",
      "quantity": 1,
      "supplierId": null,
      "pricingModel": null
    }
  ]
}
```

**SAP Response envelope:**
- nodeKey: `Item`
- Access: `$contents["Item"]` → array of price items

**SAP response item → RentalPriceCheckProduct:**
```
item["ProductID"]                        → internalId
item["NetPrice"]["DecimalValue"]          → netUnitPrice.amount
item["NetPrice"]["CurrencyCode"]          → netUnitPrice.currency
item["NetValue"]["DecimalValue"]          → netTotalPrice.amount
item["PriceComponents"][] where ConditionType="7PR1":
  ConditionRate                          → netBasePrice.amount
item["PriceComponents"][] where ConditionType="7PR6":
  ConditionValue                         → netDiscount.amount (summed if multiple)
  ConditionRate                          → discountPercentage (summed if multiple)
```

**Java SDK gaps:**
- ❌ Return type is `RentalPriceCheck` (the REQUEST model!) — should be `List<RentalPriceCheckProduct>`
- ❌ Request payload uses wrong field names (`salesUnitId` → should be `salesOrganisationId`, missing `application: "2"`)
- ❌ Response decoder can't parse `Item` node from SAP envelope
- ❌ No `RentalPriceCheckProduct` result DTO with actual price fields
- ❌ No `PriceComponents` parsing logic

---

### Method: `checkRate(RentalRateCheckInput input): RentalRateCheck`

**Route:** `POST /v1/rental-rate/post`

**SAP Request payload (from RentalRateCheckNormalizer.normalize()):**
```json
{
  "calculationMode": "1",
  "fixedReturnIndicator": false,
  "companyID": "KK-DE",
  "planningPossible": true,
  "quantity": 1
}
```
Note: `companyID` (capital D) — not `companyId`.

**SAP Response envelope:**
- Operation: `n0:RentalRateCalculatorReadByIDResponse_sync` (in IGNORED_COLLECTIONS → unwrapped)
- nodeKey: `RentalRateCalculator`
- Access: `$contents["RentalRateCalculator"]`

**SAP response → RentalRateCheck:**
```
data["SAP_UUID"]      → uuid
data["Quantity"]      → quantity (cast to int)
data["RentalRate"]    → rentalRate
data["RentalRateName"] → rentalRateName
```

**Java SDK gaps:**
- ❌ `RentalRateCheck` DTO has WRONG fields — it has request fields (calculationMode, companyId, fixedReturnIndicator, planningPossible, quantity) instead of response fields (uuid, rentalRate, rentalRateName)
- ❌ Response decoder can't handle `n0:RentalRateCalculatorReadByIDResponse_sync` envelope
- ❌ Input DTO uses `Map<String,Object>` — should be typed `RentalRateCheckInput`

---

## 3. Input DTOs — Complete Field Reference

### RentalOrderInput
| Field | Type | Default | Notes |
|---|---|---|---|
| `uuid` | String? | null | |
| `createdAt` | DateTimeInterface? | null | |
| `startDate` | DateTimeInterface? | null | |
| `endDate` | DateTimeInterface? | null | |
| `system` | String? | null | Maps to `sourceSystem` in payload |
| `name` | String? | null | |
| `buyerId` | String? | null | |
| `currency` | String | `"EUR"` | |
| `serviceOrderEnabledForFieldVu` | boolean | true | |
| `customerId` | String? | null | → `customer.internalId` |
| `serviceExecutionParty` | String? | null | → `serviceExecutionParty.internalId` |
| `salesUnitId` | String? | null | → `salesUnitParty.internalId` |
| `sellerParty` | String? | null | → `sellerParty.internalId` |
| `accountParty` | OrderParty? | null | → `accountParty` |
| `billingParty` | OrderParty? | null | → `billToParty` |
| `deliveryParty` | OrderParty? | null | → `productRecipientParty` |
| `products` | RentalOrderProduct[] | [] | |
| `employeeId` | String? | null | → `employeeResponsibleParty.internalId` |
| `deliveryBranch` | String? | null | |
| `release` | boolean | false | |
| `fieldVuDeliveryBranch` | String? | null | |

### RentalPriceCheckInput
| Field | Type | Required | Notes |
|---|---|---|---|
| `currency` | String | no (default EUR) | → `currencyCode` |
| `pricingDate` | DateTimeInterface | yes | → formatted `Y-m-d` |
| `distributionChannel` | String | yes | |
| `accountId` | String? | no | |
| `salesUnitId` | String | yes | → `salesOrganisationId` |
| `sellerParty` | String | yes | → `companyId` |
| `pricingModel` | String? | no | |
| `products` | RentalPriceCheckProductInput[] | yes | |

### RentalPriceCheckProductInput
| Field | Notes |
|---|---|
| `internalId` | → `productId` |
| `productTypeCode` | → `productTypeCode` |
| `quantity` | → `quantity` |
| `supplierId` | → `supplierId` |
| `pricingModel` | → `pricingModel` |

### RentalRateCheckInput
| Field | Notes |
|---|---|
| `calculationMode` | → `calculationMode` |
| `fixedReturnIndicator` | → `fixedReturnIndicator` |
| `companyId` | → `companyID` (capital D in SAP payload!) |
| `planningPossible` | → `planningPossible` |
| `quantity` | → `quantity` |

---

## 4. Summary — Java SDK Gaps by Severity

### 🔴 Blockers (wrong on real SAP — would return empty or crash)

| # | Gap | Impact |
|---|---|---|
| 1 | **Response decoder uses `d.results`** — SAP uses `n0:*_sync` envelope | All endpoints return empty/crash on real SAP |
| 2 | **`RentalRateCheck` DTO has request fields not response fields** | `checkRate()` always returns garbage |
| 3 | **`checkPrice()` returns `RentalPriceCheck` (request model)** | Price data completely inaccessible |
| 4 | **SerializedItem maps Java field names, not SAP field names** (`UUID` vs `internalUuid`) | All serialized items return null fields |

### 🟡 Correctness gaps (wrong API contract vs PHP)

| # | Gap | Impact |
|---|---|---|
| 5 | **No typed input DTOs** — `Map<String,Object>` instead of `RentalOrderInput` etc. | No compile-time safety, wrong field names easy |
| 6 | **checkPrice request payload wrong** — `salesUnitId` vs `salesOrganisationId`, missing `application: "2"` | SAP may reject or return wrong prices |
| 7 | **No SerializedItemValidator** | Invalid/partial SAP items passed through |
| 8 | **Missing 5 exception types** | Errors silently swallowed or wrong type thrown |

### 🟢 Missing features (not in scope for v0.1)

- SalesOrderHistory endpoint (removed in PHP v31.23)
- ShippingConditions endpoint
- Customer create/update/delete
- Package configuration
- Credit limit

---

## 5. WireMock Stub Corrections Needed

Current stubs use **Java field names** (camelCase Java DTOs). Real SAP uses **SAP field names**.

### rental.json — serialized items (should use SAP fields)
```json
{
  "UUID": "f4a1-0001",
  "VVS_UnitType": "MAT-20001",
  "VVS_UnitNumber": "SN-LH-R920-001",
  "CostCentreID": "CC-BAGGER-01",
  "VVS_EnabledForFieldVu": "true"
}
```

### rental.json — create order response (should use SAP envelope)
```json
{
  "n0:ServiceOrderByElementsResponse_synC": {
    "ServiceOrder": {
      "ID": "RO-2026-0042",
      "UUID": "a3bc-9921"
    }
  }
}
```

### rental.json — checkRate response (should use SAP envelope)
```json
{
  "n0:RentalRateCalculatorReadByIDResponse_sync": {
    "RentalRateCalculator": {
      "SAP_UUID": "rate-uuid-001",
      "Quantity": "1",
      "RentalRate": "140.00",
      "RentalRateName": "Tagessatz Bagger"
    }
  }
}
```

### rental.json — checkPrice response (Item array)
```json
{
  "Item": [
    {
      "ProductID": "MAT-20002",
      "NetPrice": { "DecimalValue": "4200.00", "CurrencyCode": "EUR" },
      "NetValue": { "DecimalValue": "4200.00", "CurrencyCode": "EUR" },
      "PriceComponents": [
        { "ConditionType": "7PR1", "ConditionRate": "4500.00" },
        { "ConditionType": "7PR6", "ConditionValue": "-300.00", "ConditionRate": "6.67" }
      ]
    }
  ]
}
```
