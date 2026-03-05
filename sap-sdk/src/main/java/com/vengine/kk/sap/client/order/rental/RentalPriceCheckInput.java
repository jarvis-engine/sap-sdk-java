package com.vengine.kk.sap.client.order.rental;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Input for checking rental prices in SAP ByDesign.
 *
 * <p>Mirrors PHP's {@code RentalPriceCheckInput}. The SAP payload uses different
 * field names — see {@code RentalOrderClient.buildPriceCheckPayload()}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentalPriceCheckInput {

    /** Maps to SAP field {@code currencyCode}. Default: EUR */
    @Builder.Default
    private String currency = "EUR";

    /** Maps to SAP field {@code pricingDate} (format: yyyy-MM-dd) */
    private LocalDate pricingDate;

    private String distributionChannel;

    /** Optional. Maps to SAP field {@code accountId} */
    private String accountId;

    /** Maps to SAP field {@code salesOrganisationId} (NOT salesUnitId!) */
    private String salesUnitId;

    /** Maps to SAP field {@code companyId} */
    private String sellerParty;

    private String pricingModel;

    @Builder.Default
    private List<RentalPriceCheckProductInput> products = new ArrayList<>();
}
