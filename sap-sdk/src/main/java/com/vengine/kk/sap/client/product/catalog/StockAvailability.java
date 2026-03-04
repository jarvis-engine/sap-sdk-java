package com.vengine.kk.sap.client.product.catalog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAvailability {

    private String productInternalId;
    private int requestedQuantity;

    @Builder.Default
    private List<SupplyPlanningArea> supplyPlanningAreas = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SupplyPlanningArea {
        private String id;
        private String name;
        private double availableQuantity;
        private String quantityTypeCode;
    }
}
