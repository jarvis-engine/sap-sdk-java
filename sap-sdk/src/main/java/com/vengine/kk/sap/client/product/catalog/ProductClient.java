package com.vengine.kk.sap.client.product.catalog;

import com.vengine.kk.sap.common.auth.SapAuthenticatedClientFactory;
import com.vengine.kk.sap.common.client.BaseSapClient;
import com.vengine.kk.sap.common.config.SapProperties;
import com.vengine.kk.sap.common.model.SapQuery;
import com.vengine.kk.sap.common.response.SapResponseDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * SAP ByDesign client for product catalog operations.
 *
 * <p>Provides 12 operations covering:
 * <ul>
 *   <li>Catalog: fetch, fetchOne, fetchCategories, fetchAttributes, fetchPackageConfigurations</li>
 *   <li>Pricing: fetchSalesPriceLists, fetchPrices, fetchPricesByPriceList</li>
 *   <li>Availability &amp; Stock: fetchAvailability, fetchStock, fetchDetails</li>
 *   <li>Non-standard (RISK-005): fetchMaterialAttribute</li>
 * </ul>
 */
@Slf4j
public class ProductClient extends BaseSapClient {

    // Standard versioned routes (appended after {baseUrl}/http/{env}/)
    private static final String PRODUCT_GET              = "v1/product/get";
    private static final String PRODUCT_GET_ONE          = "v1/product/get-one";
    private static final String PRODUCT_CATEGORIES       = "v1/product/category/get";
    private static final String PRODUCT_ATTRIBUTES       = "product-material-attribute/1.0.0/get";
    private static final String PRODUCT_DETAILS          = "v1/product/details/get";
    private static final String PRODUCT_AVAILABILITY_V1  = "v1/product/availability/get";
    private static final String PRODUCT_AVAILABILITY_V2  = "v2/product/availability/get";
    private static final String PRODUCT_STOCK_CHECK      = "v2/customer/requirement-atp-check/get";
    private static final String PRODUCT_PRICE_LISTS      = "v1/product/price-lists/ids/get";
    private static final String PRODUCT_PRICES           = "v2/product/prices/get";
    private static final String PACKAGE_CONFIG_V1        = "v1/product/packages/get";
    private static final String PACKAGE_CONFIG_V2        = "v2/product/packages/get";

    // RISK-005: non-standard route without version prefix
    private static final String MATERIAL_ATTRIBUTE       = "material-attribute/get";

    private final SapProperties sapProperties;

    public ProductClient(SapAuthenticatedClientFactory factory,
                         SapProperties properties,
                         SapResponseDecoder decoder) {
        super(factory, properties, decoder);
        this.sapProperties = properties;
    }

    // -------------------------------------------------------------------------
    // Catalog
    // -------------------------------------------------------------------------

    /**
     * Fetches a paginated list of products.
     */
    public List<Product> fetch(SapQuery query) {
        return getList(appendQueryParams(PRODUCT_GET, query != null ? query.toParamMap() : Map.of()), Product.class);
    }

    /**
     * Fetches a single product by its SAP internal ID.
     */
    public Product fetchOne(String id) {
        return get(appendQueryParams(PRODUCT_GET_ONE, Map.of("id", id)), Product.class);
    }

    /**
     * Fetches all product categories.
     */
    public List<ProductCategory> fetchCategories() {
        return getList(PRODUCT_CATEGORIES, ProductCategory.class);
    }

    /**
     * Fetches material attributes for a specific product.
     */
    public List<ProductAttribute> fetchAttributes(String productId) {
        return getList(appendQueryParams(PRODUCT_ATTRIBUTES, Map.of("productId", productId)), ProductAttribute.class);
    }

    /**
     * Fetches package/bundle configurations for a product.
     */
    public List<PackageConfiguration> fetchPackageConfigurations(String productId) {
        String route = sapProperties.getFeatures().isPackageConfigurationV2EndpointEnabled()
                ? PACKAGE_CONFIG_V2
                : PACKAGE_CONFIG_V1;
        return getList(appendQueryParams(route, Map.of("productId", productId)), PackageConfiguration.class);
    }

    // -------------------------------------------------------------------------
    // Pricing
    // -------------------------------------------------------------------------

    /**
     * Fetches the list of available sales price list identifiers.
     */
    public List<PriceList> fetchSalesPriceLists() {
        return getList(PRODUCT_PRICE_LISTS, PriceList.class);
    }

    /**
     * Fetches prices for a specific product.
     */
    public List<ProductPrice> fetchPrices(String productId) {
        return getList(appendQueryParams(PRODUCT_PRICES, Map.of("productId", productId)), ProductPrice.class);
    }

    /**
     * Fetches product prices filtered by a specific price list.
     */
    public List<ProductPrice> fetchPricesByPriceList(String priceListId) {
        return getList(appendQueryParams(PRODUCT_PRICES, Map.of("priceListId", priceListId)), ProductPrice.class);
    }

    // -------------------------------------------------------------------------
    // Availability & Stock
    // -------------------------------------------------------------------------

    /**
     * Fetches product availability for a given date range.
     */
    public ProductAvailability fetchAvailability(String productId, String fromDate, String toDate) {
        String route = sapProperties.getFeatures().isProductAvailabilityV2EndpointEnabled()
                ? PRODUCT_AVAILABILITY_V2
                : PRODUCT_AVAILABILITY_V1;
        return get(appendQueryParams(route, Map.of("productId", productId, "fromDate", fromDate, "toDate", toDate)),
                ProductAvailability.class);
    }

    /**
     * Fetches stock/ATP check for a product.
     */
    public StockAvailability fetchStock(String productId) {
        return get(appendQueryParams(PRODUCT_STOCK_CHECK, Map.of("productId", productId)), StockAvailability.class);
    }

    /**
     * Fetches detailed product information (shipping group, sortiment, etc.).
     */
    public ProductDetails fetchDetails(String productId) {
        return get(appendQueryParams(PRODUCT_DETAILS, Map.of("productId", productId)), ProductDetails.class);
    }

    // -------------------------------------------------------------------------
    // Non-standard routes (RISK-005)
    // -------------------------------------------------------------------------

    /**
     * Fetches a single material attribute for a product.
     *
     * <p><b>RISK-005:</b> This route uses a non-standard path without a version
     * segment ({@code material-attribute/get}). {@link BaseSapClient#buildUrl(String)}
     * handles this correctly.
     */
    public ProductAttribute fetchMaterialAttribute(String productId) {
        return get(appendQueryParams(MATERIAL_ATTRIBUTE, Map.of("productId", productId)), ProductAttribute.class);
    }

}
