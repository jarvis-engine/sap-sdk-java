package com.vengine.kk.sap.demo.controller;

import com.vengine.kk.sap.client.order.rental.RentalOrder;
import com.vengine.kk.sap.client.order.rental.RentalOrderClient;
import com.vengine.kk.sap.client.order.rental.RentalPriceCheck;
import com.vengine.kk.sap.client.order.rental.RentalRateCheck;
import com.vengine.kk.sap.client.product.rental.RentalProductClient;
import com.vengine.kk.sap.client.product.rental.SerializedItem;
import com.vengine.kk.sap.common.exception.SapClientException;
import com.vengine.kk.sap.common.model.SapQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Demo: SAP ByDesign Rental Flow
 *
 * Shows the full rental lifecycle as a Blubito rental-service would use it:
 *   1. List available serialized items  → rentalProductClient.fetchSerializedItems()
 *   2. Check rental price               → rentalOrderClient.checkPrice()
 *   3. Check rental rate                → rentalOrderClient.checkRate()
 *   4. Create a rental order in SAP     → rentalOrderClient.create()
 *   5. Error handling demo              → SapClientException → HTTP 502 (automatic)
 *
 * Integration (in your rental-service pom.xml):
 *
 *   <dependency>
 *     <groupId>com.vengine.kk</groupId>
 *     <artifactId>sap-sdk</artifactId>
 *     <version>0.1.2</version>
 *   </dependency>
 *
 * Config (application.yml):
 *
 *   sap:
 *     base-url: https://my-sap-instance.bydesign.cloud.sap
 *     env: prod
 *     credentials:
 *       auth-type: BASIC
 *       username: ${SAP_USER}
 *       password: ${SAP_PASS}
 */
@RestController
@RequestMapping("/demo/rental")
@Tag(name = "Rental", description = "SAP ByDesign rental lifecycle — machines, pricing, orders")
public class RentalController {

    private final RentalProductClient rentalProductClient;
    private final RentalOrderClient rentalOrderClient;

    public RentalController(RentalProductClient rentalProductClient,
                            RentalOrderClient rentalOrderClient) {
        this.rentalProductClient = rentalProductClient;
        this.rentalOrderClient = rentalOrderClient;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Step 1: List serialized items (physical machines with serial numbers)
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/items")
    @Operation(
        summary = "1. List rental machines",
        description = "Fetches all serialized items from SAP ByDesign. " +
                      "Each entry is a physical machine tracked by serial number. " +
                      "`enabled=false` = currently unavailable (maintenance, rented out, etc.).\n\n" +
                      "**Try it:** GET /demo/rental/items?limit=8"
    )
    public List<SerializedItem> listItems(
            @Parameter(description = "Max results (default 10)")
            @RequestParam(defaultValue = "10") String limit) {
        SapQuery query = new SapQuery();
        query.setLimit(limit);
        return rentalProductClient.fetchSerializedItems(query);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Step 2: Check rental price
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/price-check")
    @Operation(
        summary = "2. Check rental price",
        description = "Calculates the rental price for one or more machines over a date range. " +
                      "Returns per-product price including discounts.\n\n" +
                      "**Example body:**\n```json\n" +
                      "{\n  \"salesUnitId\": \"SU-DE-01\",\n  \"distributionChannel\": \"01\",\n" +
                      "  \"products\": [{\"internalId\": \"MAT-20002\", \"quantity\": 1,\n" +
                      "    \"startDateTime\": \"2026-04-01T08:00:00Z\",\n" +
                      "    \"endDateTime\": \"2026-04-30T17:00:00Z\"}]\n}\n```"
    )
    public RentalPriceCheck checkPrice(@RequestBody Map<String, Object> request) {
        return rentalOrderClient.checkPrice(request);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Step 3: Check rental rate
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/rate-check")
    @Operation(
        summary = "3. Check rental rate",
        description = "Fetches the applicable rental rates for a machine. " +
                      "Use this to display pricing tiers before the customer commits.\n\n" +
                      "**Example body:**\n```json\n" +
                      "{\n  \"companyId\": \"KK-DE\",\n  \"quantity\": 1,\n" +
                      "  \"calculationMode\": \"1\"\n}\n```"
    )
    public RentalRateCheck checkRate(@RequestBody Map<String, Object> request) {
        return rentalOrderClient.checkRate(request);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Step 4: Create rental order
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/orders")
    @Operation(
        summary = "4. Create rental order",
        description = "Creates a new rental order in SAP ByDesign. " +
                      "Returns the SAP-assigned `internalId` (e.g. `RO-2026-0042`) and `uuid`.\n\n" +
                      "**Example body:**\n```json\n" +
                      "{\n  \"accountId\": \"1000001\",\n  \"serialNumber\": \"SN-LH-R936-001\",\n" +
                      "  \"startDate\": \"2026-04-01\",\n  \"endDate\": \"2026-04-30\"\n}\n```"
    )
    public RentalOrder createOrder(@RequestBody Map<String, Object> request) {
        return rentalOrderClient.create(request);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Step 5: Error handling demo
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/items/error-demo")
    @Operation(
        summary = "5. Error handling demo",
        description = "Demonstrates how SAP errors are automatically mapped to clean HTTP responses. " +
                      "In production, `SapResponseErrorHandler` parses SAP OData error payloads and throws " +
                      "`SapClientException`. `GlobalExceptionHandler` maps it to HTTP 502 with structured JSON.\n\n" +
                      "**No try/catch needed in your service.** The SDK handles it.\n\n" +
                      "Expected response: `HTTP 502` with `{\"error\": \"SAP error\", \"message\": \"...\"}`"
    )
    public List<SerializedItem> errorDemo() {
        // Simulates what happens when SAP returns an error response (e.g. item not found, order blocked).
        // SapResponseErrorHandler parses the SAP error payload and throws SapClientException automatically.
        // GlobalExceptionHandler (in this demo app) catches it and returns a clean HTTP 502.
        throw new SapClientException(
            "Serialized item with serialNumber 'SN-LH-R920-UNKNOWN' does not exist in SAP ByDesign."
        );
    }
}
