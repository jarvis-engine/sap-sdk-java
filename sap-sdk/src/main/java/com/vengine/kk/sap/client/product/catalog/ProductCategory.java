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
public class ProductCategory {

    private String id;
    private String parentSectionId;
    private String name;

    @Builder.Default
    private List<String> itemAssignment = new ArrayList<>();
}
