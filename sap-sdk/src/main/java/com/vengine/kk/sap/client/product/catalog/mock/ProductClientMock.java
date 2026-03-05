package com.vengine.kk.sap.client.product.catalog.mock;

import com.vengine.kk.sap.client.product.catalog.PackageConfiguration;
import com.vengine.kk.sap.client.product.catalog.PriceList;
import com.vengine.kk.sap.client.product.catalog.Product;
import com.vengine.kk.sap.client.product.catalog.ProductAttribute;
import com.vengine.kk.sap.client.product.catalog.ProductAvailability;
import com.vengine.kk.sap.client.product.catalog.ProductCategory;
import com.vengine.kk.sap.client.product.catalog.ProductClientInterface;
import com.vengine.kk.sap.client.product.catalog.ProductDetails;
import com.vengine.kk.sap.client.product.catalog.ProductPrice;
import com.vengine.kk.sap.client.product.catalog.StockAvailability;
import com.vengine.kk.sap.common.model.SapQuery;

import java.util.Collections;
import java.util.List;

/**
 * Mock implementation of {@link ProductClientInterface} for use in tests.
 * All methods return empty collections or null by default.
 * TODO: override in tests
 */
public class ProductClientMock implements ProductClientInterface {

    public ProductClientMock() {
    }

    @Override
    public List<Product> fetch(SapQuery query) {
        // TODO: override in tests
        return Collections.emptyList();
    }

    @Override
    public Product fetchOne(String id) {
        // TODO: override in tests
        return null;
    }

    @Override
    public List<ProductCategory> fetchCategories() {
        // TODO: override in tests
        return Collections.emptyList();
    }

    @Override
    public List<ProductAttribute> fetchAttributes(String productId) {
        // TODO: override in tests
        return Collections.emptyList();
    }

    @Override
    public List<PackageConfiguration> fetchPackageConfigurations(String productId) {
        // TODO: override in tests
        return Collections.emptyList();
    }

    @Override
    public List<PriceList> fetchSalesPriceLists() {
        // TODO: override in tests
        return Collections.emptyList();
    }

    @Override
    public List<ProductPrice> fetchPrices(String productId) {
        // TODO: override in tests
        return Collections.emptyList();
    }

    @Override
    public List<ProductPrice> fetchPricesByPriceList(String priceListId) {
        // TODO: override in tests
        return Collections.emptyList();
    }

    @Override
    public ProductAvailability fetchAvailability(String productId, String fromDate, String toDate) {
        // TODO: override in tests
        return null;
    }

    @Override
    public StockAvailability fetchStock(String productId) {
        // TODO: override in tests
        return null;
    }

    @Override
    public ProductDetails fetchDetails(String productId) {
        // TODO: override in tests
        return null;
    }

    @Override
    public ProductAttribute fetchMaterialAttribute(String productId) {
        // TODO: override in tests
        return null;
    }
}
