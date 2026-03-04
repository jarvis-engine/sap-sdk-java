package com.vengine.kk.sap.common.config;

/**
 * Builds fully-qualified SAP ByDesign route URLs.
 *
 * URL pattern: {baseUrl}/http/{sapEnv}{routePath}
 */
public class SapRoutes {

    // -------------------------------------------------------------------------
    // Customer routes
    // -------------------------------------------------------------------------
    private static final String CUSTOMER_V1_POST    = "/v1/customer/post";
    private static final String CUSTOMER_V1_GET     = "/v1/customer/get";
    private static final String CUSTOMER_V1_PUT     = "/v1/customer/put";
    private static final String CUSTOMER_V1_GET_ONE = "/v1/customer/get-one";
    private static final String CUSTOMER_V1_DISCOUNTS        = "/v1/customer/discount-groups/get";
    private static final String CUSTOMER_V1_CHECK_DUPLICATES = "/v1/customer/duplicate/get";
    private static final String CUSTOMER_V1_TARGET_GROUP     = "/v1/customer/target-group/get";
    private static final String CUSTOMER_V1_CREDIT_LIMIT          = "/v1/customer/credit-limit-custom/get";
    private static final String CUSTOMER_V1_CREDIT_LIMIT_STANDARD = "/v1/customer/credit-limit/get";
    private static final String CUSTOMER_V1_DELETE_CONTACT         = "/v1/customer/relationship/delete";

    private static final String CUSTOMER_V2_POST = "/v2/customer/post";
    private static final String CUSTOMER_V2_GET  = "/v2/customer/get";
    private static final String CUSTOMER_V2_PUT  = "/v2/customer/put";

    // -------------------------------------------------------------------------
    // Employee routes
    // -------------------------------------------------------------------------
    private static final String EMPLOYEE_V1_GET = "/v1/employee/get";

    // -------------------------------------------------------------------------
    // Product routes
    // -------------------------------------------------------------------------
    private static final String PRODUCT_V1_GET              = "/v1/product/get";
    private static final String PRODUCT_V1_DETAILS          = "/v1/product/details/get";
    private static final String PRODUCT_V1_GET_ONE          = "/v1/product/get-one";
    private static final String PRODUCT_V1_ATTRIBUTE        = "/product-material-attribute/1.0.0/get";
    private static final String PRODUCT_V1_ONE_ATTRIBUTE    = "/material-attribute/get";
    private static final String PRODUCT_V1_AVAILABILITY     = "/v1/product/availability/get";
    private static final String PRODUCT_V1_SALE_PRICES      = "/v1/product/price-list/get";
    private static final String PRODUCT_V1_STOCK_CHECK      = "/v1/customer/requirement-atp-check/get";
    private static final String PRODUCT_V1_PRICE_LISTS      = "/v1/product/price-lists/ids/get";

    private static final String PRODUCT_V2_STOCK_CHECK   = "/v2/customer/requirement-atp-check/get";
    private static final String PRODUCT_V2_AVAILABILITY  = "/v2/product/availability/get";
    private static final String PRODUCT_V2_PRICES        = "/v2/product/prices/get";

    // -------------------------------------------------------------------------
    // Sales-order routes
    // -------------------------------------------------------------------------
    private static final String ORDER_V1_POST                = "/v1/sales-order/post";
    private static final String ORDER_V1_GET                 = "/v1/sales-order/get";
    private static final String ORDER_V1_CANCEL              = "/v1/sales-order/cancel";
    private static final String ORDER_V1_SHIPPING_CONDITIONS = "/v1/sales-orders-shipping-conditions/get";

    private static final String ORDER_V2_POST      = "/v2/sales-order/post";
    private static final String ORDER_V2_GET_BY_ID = "/v2/sales-order/get";

    private static final String ORDER_V3_POST = "/v3/sales-order/post";
    private static final String ORDER_V3_GET  = "/v3/sales-order/get";
    private static final String ORDER_V4_POST = "/v4/sales-order/post";
    private static final String ORDER_V4_GET  = "/v4/sales-order/get";
    private static final String ORDER_V5_POST = "/v5/sales-order/post";
    private static final String ORDER_V5_GET  = "/v5/sales-order/get";

    // -------------------------------------------------------------------------
    // Rental-order routes
    // -------------------------------------------------------------------------
    private static final String RENTAL_ORDER_V1_POST       = "/v1/service-order/post";
    private static final String RENTAL_ORDER_V1_PRICE      = "/v1/service-order/rental-price/get";
    private static final String RENTAL_ORDER_V1_RATE       = "/v1/rental-rate/post";

    // -------------------------------------------------------------------------
    // Rental-product routes
    // -------------------------------------------------------------------------
    private static final String RENTAL_PRODUCT_V1_SERIALIZED = "/v1/service-product/get";

    // -------------------------------------------------------------------------
    // Package-configuration routes
    // -------------------------------------------------------------------------
    private static final String PACKAGE_CONFIG_V1_GET = "/v1/product/packages/get";
    private static final String PACKAGE_CONFIG_V2_GET = "/v2/product/packages/get";

    // -------------------------------------------------------------------------
    // Delivery-cost routes
    // -------------------------------------------------------------------------
    private static final String DELIVERY_COST_V1_GET = "/v1/sales-order/shipping-costs/get";

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------
    private final String baseUrl;
    private final SapProperties properties;

    public SapRoutes(SapProperties properties) {
        this.properties = properties;
        this.baseUrl = String.format("%s/http/%s", properties.getBaseUrl(), properties.getEnv());
    }

    // -------------------------------------------------------------------------
    // Customer
    // -------------------------------------------------------------------------

    public String getCustomerCreateRoute() {
        return route(CUSTOMER_V2_POST);
    }

    public String getCustomerFetchRoute() {
        if (properties.getFeatures().isCustomerV2EndpointEnabled()) {
            return route(CUSTOMER_V2_GET);
        }
        return route(CUSTOMER_V1_GET);
    }

    public String getCustomerUpdateRoute() {
        return route(CUSTOMER_V2_PUT);
    }

    public String getContactDeleteRoute() {
        return route(CUSTOMER_V1_DELETE_CONTACT);
    }

    public String getCustomerFetchOneByRoute() {
        return route(CUSTOMER_V1_GET_ONE);
    }

    public String getCheckDuplicatesRoute() {
        return route(CUSTOMER_V1_CHECK_DUPLICATES);
    }

    public String getCustomerByTargetGroupRoute() {
        return route(CUSTOMER_V1_TARGET_GROUP);
    }

    public String getCreditLimitRoute() {
        return route(CUSTOMER_V1_CREDIT_LIMIT);
    }

    public String getCreditLimitStandardRoute() {
        return route(CUSTOMER_V1_CREDIT_LIMIT_STANDARD);
    }

    public String getGroupRoute() {
        return route(CUSTOMER_V1_DISCOUNTS);
    }

    // -------------------------------------------------------------------------
    // Employee
    // -------------------------------------------------------------------------

    public String getEmployeeFetchRoute() {
        return route(EMPLOYEE_V1_GET);
    }

    // -------------------------------------------------------------------------
    // Product
    // -------------------------------------------------------------------------

    public String getProductFetchRoute() {
        return route(PRODUCT_V1_GET);
    }

    public String getOneProductFetchRoute() {
        return route(PRODUCT_V1_GET_ONE);
    }

    public String getProductAttributesFetchRoute() {
        return route(PRODUCT_V1_ATTRIBUTE);
    }

    public String getOneProductAttributesFetchRoute() {
        return route(PRODUCT_V1_ONE_ATTRIBUTE);
    }

    public String getMultipleProductAvailabilityFetchRoute() {
        if (properties.getFeatures().isProductAvailabilityV2EndpointEnabled()) {
            return route(PRODUCT_V2_AVAILABILITY);
        }
        return route(PRODUCT_V1_AVAILABILITY);
    }

    /** @deprecated Use {@link #getSalesPriceListsRoute()} instead */
    @Deprecated
    public String getProductSalesPriceListRoute() {
        return route(PRODUCT_V1_SALE_PRICES);
    }

    public String getSalesPriceListsRoute() {
        return route(PRODUCT_V1_PRICE_LISTS);
    }

    public String getSalesPricesByPriceList() {
        return route(PRODUCT_V2_PRICES);
    }

    public String getProductStockCheckRoute() {
        return route(PRODUCT_V2_STOCK_CHECK);
    }

    public String getProductDetailsRoute() {
        return route(PRODUCT_V1_DETAILS);
    }

    public String getPackageConfigurationRoute() {
        if (properties.getFeatures().isPackageConfigurationV2EndpointEnabled()) {
            return route(PACKAGE_CONFIG_V2_GET);
        }
        return route(PACKAGE_CONFIG_V1_GET);
    }

    // -------------------------------------------------------------------------
    // Sales order
    // -------------------------------------------------------------------------

    public String getOrderCreateRoute() {
        String path = ORDER_V2_POST;
        if (properties.getFeatures().isSalesOrderV3EndpointEnabled()) {
            path = ORDER_V3_POST;
        }
        if (properties.getFeatures().isSalesOrderV4EndpointEnabled()) {
            path = ORDER_V4_POST;
        }
        if (properties.getFeatures().isSalesOrderV5EndpointEnabled()) {
            path = ORDER_V5_POST;
        }
        return route(path);
    }

    public String getOrderUpdateRoute() {
        return getOrderCreateRoute();
    }

    public String getOrderFetchRoute() {
        String path = ORDER_V1_GET;
        if (properties.getFeatures().isSalesOrderV3EndpointEnabled()) {
            path = ORDER_V3_GET;
        }
        if (properties.getFeatures().isSalesOrderV4EndpointEnabled()) {
            path = ORDER_V4_GET;
        }
        if (properties.getFeatures().isSalesOrderV5EndpointEnabled()) {
            path = ORDER_V5_GET;
        }
        return route(path);
    }

    public String getOrderByIdRoute() {
        return route(ORDER_V2_GET_BY_ID);
    }

    public String getOrderCancelRoute() {
        return route(ORDER_V1_CANCEL);
    }

    public String getOrdersShippingConditions() {
        return route(ORDER_V1_SHIPPING_CONDITIONS);
    }

    // -------------------------------------------------------------------------
    // Rental order
    // -------------------------------------------------------------------------

    public String getRentalOrderCreateRoute() {
        return route(RENTAL_ORDER_V1_POST);
    }

    public String getRentalPriceCheckRoute() {
        return route(RENTAL_ORDER_V1_PRICE);
    }

    public String getRentalRateCheckRoute() {
        return route(RENTAL_ORDER_V1_RATE);
    }

    // -------------------------------------------------------------------------
    // Rental product
    // -------------------------------------------------------------------------

    public String getSerializedItems() {
        return route(RENTAL_PRODUCT_V1_SERIALIZED);
    }

    // -------------------------------------------------------------------------
    // Delivery cost
    // -------------------------------------------------------------------------

    public String getDeliveryCostFetchRoute() {
        return route(DELIVERY_COST_V1_GET);
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private String route(String routePath) {
        return baseUrl + routePath;
    }
}
