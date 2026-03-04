package com.vengine.kk.sap.client.order.sales;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderProduct {

    private String internalId;
    private String quantity;

    @Nullable
    private String deliveryStatus;

    @Nullable
    private String basePriceAmount;

    @Nullable
    private String basePriceCurrency;

    @Nullable
    private String priceAmount;

    @Nullable
    private String priceCurrency;

    @Nullable
    private String discountAmount;

    @Nullable
    private String discountCurrency;

    @Nullable
    private String discountPercent;

    @Nullable
    private String taxAmount;

    @Nullable
    private String taxCurrency;

    /** Direct-delivery flag (e.g. "1" = yes, "2" = no). */
    @Nullable
    private String directDelivery;
}
