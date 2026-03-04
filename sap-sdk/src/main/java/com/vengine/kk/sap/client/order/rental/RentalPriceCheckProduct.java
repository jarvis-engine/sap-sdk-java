package com.vengine.kk.sap.client.order.rental;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentalPriceCheckProduct {

    private String internalId;

    /** Net unit price amount */
    private String netUnitPriceAmount;
    private String netUnitPriceCurrency;

    /** Net total price amount */
    private String netTotalPriceAmount;
    private String netTotalPriceCurrency;

    /** Net base price amount */
    private String netBasePriceAmount;
    private String netBasePriceCurrency;

    @Nullable
    private String netDiscountAmount;

    @Nullable
    private String netDiscountCurrency;

    @Nullable
    private String discountPercentage;
}
