package com.vengine.kk.sap.client.order.sales;

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
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@EnableWireMock({
        @ConfigureWireMock(name = "sap", property = "sap.base-url")
})
@TestPropertySource(properties = "sap.features.sales-order-v3-endpoint-enabled=true")
class SalesOrderClientV3IntegrationTest {

    @InjectWireMock("sap")
    private WireMockServer wireMock;

    @Autowired
    private SalesOrderClient salesOrderClient;

    @BeforeEach
    void resetStubs() {
        wireMock.resetAll();
    }

    @Test
    void fetchUsesV3WhenFlagEnabled() {
        wireMock.stubFor(get(urlPathEqualTo("/http/test/v3/sales-order/get"))
                .willReturn(okJson("""
                        {"d": {"results": [{"internalId": "SO-V3", "uuid": "v3-uuid", "status": "Open"}]}}
                        """)));

        List<SalesOrder> orders = salesOrderClient.fetch(null);
        assertThat(orders).hasSize(1);
        wireMock.verify(getRequestedFor(urlPathEqualTo("/http/test/v3/sales-order/get")));
    }

    @Test
    void createUsesV3WhenFlagEnabled() {
        wireMock.stubFor(post(urlPathEqualTo("/http/test/v3/sales-order/post"))
                .willReturn(okJson("""
                        {"d": {"internalId": "SO-V3", "uuid": "v3-uuid", "status": "New"}}
                        """)));

        SalesOrder order = salesOrderClient.create(Map.of("accountId", "ACC-001"));
        assertThat(order.getInternalId()).isEqualTo("SO-V3");
        wireMock.verify(postRequestedFor(urlPathEqualTo("/http/test/v3/sales-order/post")));
    }
}
