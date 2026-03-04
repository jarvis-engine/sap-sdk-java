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
public class ProductCollection {

    @Builder.Default
    private List<Product> items = new ArrayList<>();

    @Builder.Default
    private List<ProductCategory> categories = new ArrayList<>();
}
