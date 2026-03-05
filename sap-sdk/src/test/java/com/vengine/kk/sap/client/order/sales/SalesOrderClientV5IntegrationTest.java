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
@TestPropertySource(properties = {
        "sap.features.sales-order-v4-endpoint-enabled=true",
        "sap.features.sales-order-v5-endpoint-enabled=true"
})
class SalesOrderClientV5IntegrationTest {

    @InjectWireMock("sap")
    private WireMockServer wireMock;

    @Autowired
    private SalesOrderClient salesOrderClient;

    @BeforeEach
    void resetStubs() {
        wireMock.resetAll();
    }

    @Test
    void fetchUsesV5WhenV4AndV5Enabled() {
        wireMock.stubFor(get(urlPathEqualTo("/http/test/v5/sales-order/get"))
                .willReturn(okJson("""
                        {"d": {"results": [{"internalId": "SO-V5", "uuid": "v5-uuid", "status": "Open"}]}}
                        """)));

        List<SalesOrder> orders = salesOrderClient.fetch(null);
        assertThat(orders).hasSize(1);
        wireMock.verify(getRequestedFor(urlPathEqualTo("/http/test/v5/sales-order/get")));
    }

    @Test
    void createUsesV5WhenV4AndV5Enabled() {
        wireMock.stubFor(post(urlPathEqualTo("/http/test/v5/sales-order/post"))
                .willReturn(okJson("""
                        {"d": {"internalId": "SO-V5", "uuid": "v5-uuid", "status": "New"}}
                        """)));

        SalesOrder order = salesOrderClient.create(Map.of("accountId", "ACC-001"));
        assertThat(order.getInternalId()).isEqualTo("SO-V5");
        wireMock.verify(postRequestedFor(urlPathEqualTo("/http/test/v5/sales-order/post")));
    }
}
