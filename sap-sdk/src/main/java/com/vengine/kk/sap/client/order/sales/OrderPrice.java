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
public class OrderPrice {

    private String typeCode;
    private String description;
    private String descriptionLanguage;
    private String categoryCode;
    private String purposeCode;

    /** Amount value as string (e.g. "100.00") */
    private String priceAmount;
    private String priceCurrency;

    @Nullable
    private String calculationBasisAmount;

    @Nullable
    private String calculationBasisCurrency;
}
