package com.vengine.kk.sap.client.product.catalog;

import com.vengine.kk.sap.common.model.SapQuery;

import java.util.List;

/**
 * Interface for SAP ByDesign Product Catalog operations.
 * Implement this interface in tests to mock SAP product calls.
 */
public interface ProductClientInterface {

    List<Product> fetch(SapQuery query);

    Product fetchOne(String id);

    List<ProductCategory> fetchCategories();

    List<ProductAttribute> fetchAttributes(String productId);

    List<PackageConfiguration> fetchPackageConfigurations(String productId);

    List<PriceList> fetchSalesPriceLists();

    List<ProductPrice> fetchPrices(String productId);

    List<ProductPrice> fetchPricesByPriceList(String priceListId);

    ProductAvailability fetchAvailability(String productId, String fromDate, String toDate);

    StockAvailability fetchStock(String productId);

    ProductDetails fetchDetails(String productId);

    ProductAttribute fetchMaterialAttribute(String productId);
}
