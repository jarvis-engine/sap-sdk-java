package com.vengine.kk.sap.client.product.catalog;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.maciejwalkowiak.wiremock.spring.ConfigureWireMock;
import com.maciejwalkowiak.wiremock.spring.EnableWireMock;
import com.maciejwalkowiak.wiremock.spring.InjectWireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@EnableWireMock({
        @ConfigureWireMock(name = "sap", property = "sap.base-url")
})
class ProductClientIntegrationTest {

    @InjectWireMock("sap")
    private WireMockServer wireMock;

    @Autowired
    private ProductClient productClient;

    @BeforeEach
    void resetStubs() {
        wireMock.resetAll();
    }

    @Test
    void fetchReturnsProductList() {
        wireMock.stubFor(get(urlPathEqualTo("/http/test/v1/product/get"))
                .willReturn(okJson("""
                        {"d": {"results": [
                            {"id": "P001", "productId": "MAT-001", "name": "Widget A"},
                            {"id": "P002", "productId": "MAT-002", "name": "Widget B"}
                        ]}}
                        """)));

        List<Product> products = productClient.fetch(null);
        assertThat(products).hasSize(2);
        assertThat(products.get(0).getName()).isEqualTo("Widget A");
    }

    @Test
    void fetchOneReturnsSingleProduct() {
        wireMock.stubFor(get(urlPathEqualTo("/http/test/v1/product/get-one"))
                .withQueryParam("id", equalTo("MAT-001"))
                .willReturn(okJson("""
                        {"d": {"id": "P001", "productId": "MAT-001", "name": "Widget A"}}
                        """)));

        Product product = productClient.fetchOne("MAT-001");
        assertThat(product.getProductId()).isEqualTo("MAT-001");
        assertThat(product.getName()).isEqualTo("Widget A");
    }

    @Test
    void fetchCategoriesReturnsCategoryList() {
        wireMock.stubFor(get(urlPathEqualTo("/http/test/v1/product/category/get"))
                .willReturn(okJson("""
                        {"d": {"results": [
                            {"id": "CAT1", "parentSectionId": "", "name": "Electronics"},
                            {"id": "CAT2", "parentSectionId": "CAT1", "name": "Phones"}
                        ]}}
                        """)));

        List<ProductCategory> categories = productClient.fetchCategories();
        assertThat(categories).hasSize(2);
        assertThat(categories.get(1).getParentSectionId()).isEqualTo("CAT1");
    }

    @Test
    void fetchPackageConfigurationsV1ByDefault() {
        wireMock.stubFor(get(urlPathEqualTo("/http/test/v1/product/packages/get"))
                .withQueryParam("productId", equalTo("MAT-001"))
                .willReturn(okJson("""
                        {"d": {"results": [{"id": "PKG1", "productId": "MAT-001", "quantity": 10}]}}
                        """)));

        List<PackageConfiguration> configs = productClient.fetchPackageConfigurations("MAT-001");
        assertThat(configs).hasSize(1);
        assertThat(configs.get(0).getProductId()).isEqualTo("MAT-001");
    }

    @Test
    void fetchAvailabilityV1ByDefault() {
        wireMock.stubFor(get(urlPathEqualTo("/http/test/v1/product/availability/get"))
                .withQueryParam("productId", equalTo("MAT-001"))
                .willReturn(okJson("""
                        {"d": {"productInternalId": "MAT-001", "currentStockQuantity": 100.0, "availableQuantity": 80.0}}
                        """)));

        ProductAvailability avail = productClient.fetchAvailability("MAT-001", "2026-01-01", "2026-12-31");
        assertThat(avail.getProductInternalId()).isEqualTo("MAT-001");
        assertThat(avail.getAvailableQuantity()).isEqualTo(80.0);
    }

    @Test
    void fetchSalesPriceListsReturnsList() {
        wireMock.stubFor(get(urlPathEqualTo("/http/test/v1/product/price-lists/ids/get"))
                .willReturn(okJson("""
                        {"d": {"results": [{"code": "PL-001"}, {"code": "PL-002"}]}}
                        """)));

        List<PriceList> lists = productClient.fetchSalesPriceLists();
        assertThat(lists).hasSize(2);
        assertThat(lists.get(0).getCode()).isEqualTo("PL-001");
    }

    @Test
    void fetchPricesReturnsPricesForProduct() {
        wireMock.stubFor(get(urlPathEqualTo("/http/test/v2/product/prices/get"))
                .withQueryParam("productId", equalTo("MAT-001"))
                .willReturn(okJson("""
                        {"d": {"results": [{"productId": "MAT-001", "priceAmount": "49.99", "priceCurrency": "EUR"}]}}
                        """)));

        List<ProductPrice> prices = productClient.fetchPrices("MAT-001");
        assertThat(prices).hasSize(1);
        assertThat(prices.get(0).getPriceAmount()).isEqualTo("49.99");
    }

    @Test
    void fetchMaterialAttributeHitsNonStandardRoute() {
        wireMock.stubFor(get(urlPathEqualTo("/http/test/material-attribute/get"))
                .withQueryParam("productId", equalTo("MAT-001"))
                .willReturn(okJson("""
                        {"d": {"uuid": "attr-uuid", "productId": "MAT-001", "value": "Steel"}}
                        """)));

        ProductAttribute attr = productClient.fetchMaterialAttribute("MAT-001");
        assertThat(attr.getUuid()).isEqualTo("attr-uuid");
        assertThat(attr.getValue()).isEqualTo("Steel");
    }
}
