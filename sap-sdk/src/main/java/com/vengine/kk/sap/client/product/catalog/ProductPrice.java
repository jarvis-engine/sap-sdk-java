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
public class ProductPrice {

    private String productId;
    private String priceAmount;
    private String priceCurrency;

    @Builder.Default
    private List<PriceList> priceLists = new ArrayList<>();
}
