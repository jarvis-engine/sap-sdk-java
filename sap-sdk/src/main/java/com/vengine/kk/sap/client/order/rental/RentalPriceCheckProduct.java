package com.vengine.kk.sap.client.order.rental;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A single product result from a rental price check.
 *
 * <p>Returned as a list by {@link RentalOrderClient#checkPrice(RentalPriceCheckInput)}.
 * Built by parsing the raw SAP {@code Item} array — see client for mapping logic.
 *
 * <p>PHP equivalent: {@code RentalPriceCheckProduct} (from {@code RentalPriceCheckNormalizer.denormalize()}).
 *
 * <p>SAP source fields per item:
 * <ul>
 *   <li>{@code ProductID} → internalId
 *   <li>{@code NetPrice.DecimalValue} + {@code NetPrice.CurrencyCode} → netUnitPrice*
 *   <li>{@code NetValue.DecimalValue} → netTotalPrice*
 *   <li>PriceComponents ConditionType=7PR1 ConditionRate → netBasePrice*
 *   <li>PriceComponents ConditionType=7PR6 ConditionValue → netDiscount*, ConditionRate → discountPercentage
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentalPriceCheckProduct {

    private String internalId;
    private String currency;

    private String netUnitPriceAmount;
    private String netTotalPriceAmount;
    private String netBasePriceAmount;
    private String netDiscountAmount;

    /** Percentage as string, e.g. "6.67". Null if no discount. */
    private String discountPercentage;
}
