package com.vengine.kk.sap.client.product.catalog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductAvailability {

    private String productInternalId;
    private String productTypeCode;
    private String productTypeName;
    private String productTypeNameLanguage;
    private String supplyPlanningAreaId;

    private double currentStockQuantity;
    private String currentStockQuantityTypeCode;

    private double requirementQuantity;
    private String requirementQuantityTypeCode;

    private double receiptQuantity;
    private String receiptQuantityTypeCode;

    private double availableQuantity;
    private String availableQuantityTypeCode;
}
