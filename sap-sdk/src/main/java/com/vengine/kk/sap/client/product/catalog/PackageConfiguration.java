package com.vengine.kk.sap.client.product.catalog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Product package / bundle configuration.
 * Maps from PHP {@code ProductPackage}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageConfiguration {

    private String id;
    private String productId;
    private String correspondingQuantityTypeCode;
    private int correspondingQuantity;
    private String quantityTypeCode;
    private int quantity;
}
