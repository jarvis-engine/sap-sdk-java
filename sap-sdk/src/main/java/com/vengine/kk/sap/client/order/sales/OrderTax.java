package com.vengine.kk.sap.client.order.sales;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderTax {

    private String typeCode;
    private String description;
    private String descriptionLanguage;
    private String categoryCode;
    private String purposeCode;

    private String priceAmount;
    private String priceCurrency;

    private String countryCode;
    private String eventTypeCode;
    private String basePriceAmount;
    private String basePriceCurrency;
    private String percent;
}
