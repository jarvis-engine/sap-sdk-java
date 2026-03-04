package com.vengine.kk.sap.client.product.catalog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    private String id;
    private String productId;
    private String name;

    @Nullable
    private String description;

    private int productTypeCode;
    private String measureUnit;
    private int orderMinimumQuantity;

    private String amountGross;
    private String currencyGross;
    private String amountNet;
    private String currencyNet;

    private int baseQuantity;
    private String baseQuantityTypeCode;

    @Builder.Default
    private List<ProductAttribute> attributes = new ArrayList<>();

    @Nullable
    private String category;

    @Nullable
    private ProductAvailability productAvailability;

    @Nullable
    private String sortiment;

    @Nullable
    private String shippingGroup;
}
