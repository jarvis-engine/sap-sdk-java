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
@TestPropertySource(properties = {
        "sap.features.package-configuration-v2-endpoint-enabled=true",
        "sap.features.product-availability-v2-endpoint-enabled=true"
})
class ProductClientV2IntegrationTest {

    @InjectWireMock("sap")
    private WireMockServer wireMock;

    @Autowired
    private ProductClient productClient;

    @BeforeEach
    void resetStubs() {
        wireMock.resetAll();
    }

    @Test
    void fetchPackageConfigurationsV2WhenFlagEnabled() {
        wireMock.stubFor(get(urlPathEqualTo("/http/test/v2/product/packages/get"))
                .withQueryParam("productId", equalTo("MAT-001"))
                .willReturn(okJson("""
                        {"d": {"results": [{"id": "PKG-V2", "productId": "MAT-001", "quantity": 20}]}}
                        """)));

        List<PackageConfiguration> configs = productClient.fetchPackageConfigurations("MAT-001");
        assertThat(configs).hasSize(1);
        assertThat(configs.get(0).getId()).isEqualTo("PKG-V2");

        wireMock.verify(getRequestedFor(urlPathEqualTo("/http/test/v2/product/packages/get")));
    }

    @Test
    void fetchAvailabilityV2WhenFlagEnabled() {
        wireMock.stubFor(get(urlPathEqualTo("/http/test/v2/product/availability/get"))
                .withQueryParam("productId", equalTo("MAT-001"))
                .willReturn(okJson("""
                        {"d": {"productInternalId": "MAT-001", "currentStockQuantity": 200.0, "availableQuantity": 150.0}}
                        """)));

        ProductAvailability avail = productClient.fetchAvailability("MAT-001", "2026-01-01", "2026-12-31");
        assertThat(avail.getAvailableQuantity()).isEqualTo(150.0);

        wireMock.verify(getRequestedFor(urlPathEqualTo("/http/test/v2/product/availability/get")));
    }
}
