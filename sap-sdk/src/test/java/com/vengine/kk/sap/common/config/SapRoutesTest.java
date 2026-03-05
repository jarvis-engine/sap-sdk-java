package com.vengine.kk.sap.common.config;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class SapRoutesTest {

    private SapRoutes createRoutes(boolean customerV2, boolean v3, boolean v4, boolean v5,
                                    boolean pkgV2, boolean availV2) {
        SapProperties props = new SapProperties();
        props.setBaseUrl("https://sap.example.com");
        props.setEnv("test");
        SapProperties.Features features = new SapProperties.Features();
        features.setCustomerV2EndpointEnabled(customerV2);
        features.setSalesOrderV3EndpointEnabled(v3);
        features.setSalesOrderV4EndpointEnabled(v4);
        features.setSalesOrderV5EndpointEnabled(v5);
        features.setPackageConfigurationV2EndpointEnabled(pkgV2);
        features.setProductAvailabilityV2EndpointEnabled(availV2);
        props.setFeatures(features);
        return new SapRoutes(props);
    }

    @Test
    void customerFetchRouteV1WhenFlagOff() {
        SapRoutes routes = createRoutes(false, false, false, false, false, false);
        assertThat(routes.getCustomerFetchRoute())
                .isEqualTo("https://sap.example.com/http/test/v1/customer/get");
    }

    @Test
    void customerFetchRouteV2WhenFlagOn() {
        SapRoutes routes = createRoutes(true, false, false, false, false, false);
        assertThat(routes.getCustomerFetchRoute())
                .isEqualTo("https://sap.example.com/http/test/v2/customer/get");
    }

    @Test
    void orderCreateRouteDefaultV2() {
        SapRoutes routes = createRoutes(false, false, false, false, false, false);
        assertThat(routes.getOrderCreateRoute())
                .isEqualTo("https://sap.example.com/http/test/v2/sales-order/post");
    }

    @Test
    void orderCreateRouteV3WhenEnabled() {
        SapRoutes routes = createRoutes(false, true, false, false, false, false);
        assertThat(routes.getOrderCreateRoute())
                .isEqualTo("https://sap.example.com/http/test/v3/sales-order/post");
    }

    @Test
    void orderCreateRouteV4WhenEnabled() {
        SapRoutes routes = createRoutes(false, false, true, false, false, false);
        assertThat(routes.getOrderCreateRoute())
                .isEqualTo("https://sap.example.com/http/test/v4/sales-order/post");
    }

    @Test
    void orderCreateRouteV5WhenEnabled() {
        SapRoutes routes = createRoutes(false, false, false, true, false, false);
        assertThat(routes.getOrderCreateRoute())
                .isEqualTo("https://sap.example.com/http/test/v5/sales-order/post");
    }

    @Test
    void orderCreateRouteV5TakesPrecedenceOverV3AndV4() {
        SapRoutes routes = createRoutes(false, true, true, true, false, false);
        assertThat(routes.getOrderCreateRoute())
                .isEqualTo("https://sap.example.com/http/test/v5/sales-order/post");
    }

    @Test
    void allRoutesStartWithBaseUrlHttpEnv() {
        SapRoutes routes = createRoutes(false, false, false, false, false, false);
        String prefix = "https://sap.example.com/http/test/";

        // Use reflection to invoke all public get*Route methods
        List<String> urls = getAllRouteUrls(routes);
        assertThat(urls).isNotEmpty();
        for (String url : urls) {
            assertThat(url).startsWith(prefix);
        }
    }

    @Test
    void noDoubleSlashesInAnyUrl() {
        SapRoutes routes = createRoutes(true, true, true, true, true, true);

        List<String> urls = getAllRouteUrls(routes);
        for (String url : urls) {
            // After the protocol (https://), there should be no double slashes
            String afterProtocol = url.substring(url.indexOf("://") + 3);
            assertThat(afterProtocol).doesNotContain("//");
        }
    }

    @Test
    void packageConfigurationRouteRespectsFlag() {
        SapRoutes routesV1 = createRoutes(false, false, false, false, false, false);
        assertThat(routesV1.getPackageConfigurationRoute()).contains("/v1/product/packages/get");

        SapRoutes routesV2 = createRoutes(false, false, false, false, true, false);
        assertThat(routesV2.getPackageConfigurationRoute()).contains("/v2/product/packages/get");
    }

    @Test
    void productAvailabilityRouteRespectsFlag() {
        SapRoutes routesV1 = createRoutes(false, false, false, false, false, false);
        assertThat(routesV1.getMultipleProductAvailabilityFetchRoute()).contains("/v1/product/availability/get");

        SapRoutes routesV2 = createRoutes(false, false, false, false, false, true);
        assertThat(routesV2.getMultipleProductAvailabilityFetchRoute()).contains("/v2/product/availability/get");
    }

    private List<String> getAllRouteUrls(SapRoutes routes) {
        List<String> urls = new ArrayList<>();
        for (Method method : SapRoutes.class.getDeclaredMethods()) {
            if (method.getParameterCount() == 0
                    && method.getReturnType() == String.class
                    && (method.getName().startsWith("get") || method.getName().startsWith("route"))) {
                try {
                    method.setAccessible(true);
                    String url = (String) method.invoke(routes);
                    if (url != null) urls.add(url);
                } catch (Exception ignored) {
                }
            }
        }
        return urls;
    }
}
