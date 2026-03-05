package com.vengine.kk.sap.client.order.rental;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A product line item within a {@link RentalPriceCheckInput}.
 * Mirrors PHP's {@code RentalPriceCheckProductInput}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentalPriceCheckProductInput {

    /** Maps to SAP field {@code productId} */
    private String internalId;

    /** Maps to SAP field {@code productTypeCode} */
    private String productTypeCode;

    private int quantity;

    /** Maps to SAP field {@code supplierId} */
    private String supplierId;

    private String pricingModel;
}
