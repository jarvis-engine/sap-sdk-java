package com.vengine.kk.sap.demo.controller;

import com.vengine.kk.sap.client.order.rental.*;
import com.vengine.kk.sap.client.product.rental.RentalProductClient;
import com.vengine.kk.sap.client.product.rental.SerializedItem;
import com.vengine.kk.sap.common.exception.SapClientException;
import com.vengine.kk.sap.common.model.SapQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Demo: SAP ByDesign Rental Flow
 *
 * Shows the full rental lifecycle as Blubito's rental-service would use it.
 * All requests hit WireMock (pre-loaded with real SAP response envelope formats).
 *
 * Integration in rental-service pom.xml:
 *
 *   <dependency>
 *     <groupId>com.vengine.kk</groupId>
 *     <artifactId>sap-sdk</artifactId>
 *     <version>0.1.2</version>
 *   </dependency>
 *
 * application.yml:
 *
 *   sap:
 *     base-url: https://your-sap-instance.bydesign.cloud.sap
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

    @GetMapping("/items")
    @Operation(
        summary = "1. List rental machines",
        description = "Fetches serialized items from SAP (node: FixedAsset). " +
                      "Each item = a physical machine with serial number. " +
                      "`enabled=false` (VVS_EnabledForFieldVu≠'true') = unavailable. " +
                      "Items missing required SAP fields are automatically filtered."
    )
    public List<SerializedItem> listItems(
            @Parameter(description = "Max results")
            @RequestParam(defaultValue = "10") String limit) {
        SapQuery query = new SapQuery();
        query.setLimit(limit);
        return rentalProductClient.fetchSerializedItems(query);
    }

    @PostMapping("/price-check")
    @Operation(
        summary = "2. Check rental price",
        description = "Calculates rental prices. SAP response parsed from Item[] array with " +
                      "PriceComponents (7PR1=base price, 7PR6=discount).\n\n" +
                      "**Example:**\n```json\n{\n" +
                      "  \"salesUnitId\": \"SU-DE-01\",\n" +
                      "  \"sellerParty\": \"KK-DE\",\n" +
                      "  \"distributionChannel\": \"01\",\n" +
                      "  \"pricingDate\": \"2026-04-01\",\n" +
                      "  \"products\": [{\"internalId\": \"MAT-20002\", \"quantity\": 1}]\n}\n```"
    )
    public List<RentalPriceCheckProduct> checkPrice(@RequestBody RentalPriceCheckInput input) {
        return rentalOrderClient.checkPrice(input);
    }

    @PostMapping("/rate-check")
    @Operation(
        summary = "3. Check rental rate",
        description = "Fetches rate tiers from SAP (node: RentalRateCalculator). " +
                      "Returns uuid, rentalRate, rentalRateName.\n\n" +
                      "**Example:**\n```json\n" +
                      "{\"companyId\": \"KK-DE\", \"quantity\": 1}\n```"
    )
    public RentalRateCheck checkRate(@RequestBody RentalRateCheckInput input) {
        return rentalOrderClient.checkRate(input);
    }

    @PostMapping("/orders")
    @Operation(
        summary = "4. Create rental order",
        description = "Creates a service order in SAP (envelope: n0:ServiceOrderByElementsResponse_synC). " +
                      "Returns SAP-assigned ID and UUID.\n\n" +
                      "**Example:**\n```json\n{\n" +
                      "  \"customerId\": \"1000001\",\n" +
                      "  \"sellerParty\": \"KK-DE\",\n" +
                      "  \"salesUnitId\": \"SU-DE-01\",\n" +
                      "  \"currency\": \"EUR\"\n}\n```"
    )
    public RentalOrder createOrder(@RequestBody RentalOrderInput input) {
        return rentalOrderClient.create(input);
    }

    @GetMapping("/items/error-demo")
    @Operation(
        summary = "5. Error handling demo",
        description = "Shows how SapClientException is automatically mapped to HTTP 502. " +
                      "No try/catch needed in your service — GlobalExceptionHandler handles it."
    )
    public List<SerializedItem> errorDemo() {
        throw new SapClientException(
            "Serialized item with serialNumber 'SN-LH-R920-UNKNOWN' does not exist in SAP ByDesign."
        );
    }

    @PostMapping("/price-check/example")
    @Operation(
        summary = "2b. Price check — pre-filled example",
        description = "Runs a price check with pre-filled example data so you can try it without a request body."
    )
    public List<RentalPriceCheckProduct> checkPriceExample() {
        RentalPriceCheckInput input = RentalPriceCheckInput.builder()
            .salesUnitId("SU-DE-01")
            .sellerParty("KK-DE")
            .distributionChannel("01")
            .pricingDate(LocalDate.of(2026, 4, 1))
            .products(List.of(
                RentalPriceCheckProductInput.builder()
                    .internalId("MAT-20002")
                    .quantity(1)
                    .build()
            ))
            .build();
        return rentalOrderClient.checkPrice(input);
    }

    @PostMapping("/rate-check/example")
    @Operation(
        summary = "3b. Rate check — pre-filled example",
        description = "Runs a rate check with pre-filled example data."
    )
    public RentalRateCheck checkRateExample() {
        RentalRateCheckInput input = RentalRateCheckInput.builder()
            .companyId("KK-DE")
            .quantity(1)
            .build();
        return rentalOrderClient.checkRate(input);
    }

    @PostMapping("/orders/example")
    @Operation(
        summary = "4b. Create order — pre-filled example",
        description = "Creates a rental order with pre-filled example data."
    )
    public RentalOrder createOrderExample() {
        RentalOrderInput input = RentalOrderInput.builder()
            .customerId("1000001")
            .sellerParty("KK-DE")
            .salesUnitId("SU-DE-01")
            .currency("EUR")
            .build();
        return rentalOrderClient.create(input);
    }
}
