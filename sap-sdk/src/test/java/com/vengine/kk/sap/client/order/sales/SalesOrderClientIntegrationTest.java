package com.vengine.kk.sap.client.order.sales;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.maciejwalkowiak.wiremock.spring.ConfigureWireMock;
import com.maciejwalkowiak.wiremock.spring.EnableWireMock;
import com.maciejwalkowiak.wiremock.spring.InjectWireMock;
import com.vengine.kk.sap.common.model.SapQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@EnableWireMock({
        @ConfigureWireMock(name = "sap", property = "sap.base-url")
})
class SalesOrderClientIntegrationTest {

    @InjectWireMock("sap")
    private WireMockServer wireMock;

    @Autowired
    private SalesOrderClient salesOrderClient;

    private static final String ORDER_JSON = """
            {"d": {"results": [
                {"internalId": "SO-001", "uuid": "so-uuid-1", "status": "Open"}
            ]}}
            """;

    private static final String SINGLE_ORDER_JSON = """
            {"d": {"internalId": "SO-001", "uuid": "so-uuid-1", "status": "Open"}}
            """;

    @BeforeEach
    void resetStubs() {
        wireMock.resetAll();
    }

    // ── fetch ───────────────────────────────────────────────────────────────

    @Test
    void fetchUsesV1ByDefault() {
        wireMock.stubFor(get(urlPathEqualTo("/http/test/v1/sales-order/get"))
                .willReturn(okJson(ORDER_JSON)));

        List<SalesOrder> orders = salesOrderClient.fetch(null);
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getInternalId()).isEqualTo("SO-001");

        wireMock.verify(getRequestedFor(urlPathEqualTo("/http/test/v1/sales-order/get")));
    }

    // ── fetchOne ────────────────────────────────────────────────────────────

    @Test
    void fetchOneUsesV2Endpoint() {
        wireMock.stubFor(get(urlPathEqualTo("/http/test/v2/sales-order/get"))
                .withQueryParam("id", equalTo("SO-001"))
                .willReturn(okJson(SINGLE_ORDER_JSON)));

        SalesOrder order = salesOrderClient.fetchOne("SO-001");
        assertThat(order.getInternalId()).isEqualTo("SO-001");

        wireMock.verify(getRequestedFor(urlPathEqualTo("/http/test/v2/sales-order/get")));
    }

    // ── create ──────────────────────────────────────────────────────────────

    @Test
    void createUsesV2ByDefault() {
        wireMock.stubFor(post(urlPathEqualTo("/http/test/v2/sales-order/post"))
                .willReturn(okJson(SINGLE_ORDER_JSON)));

        SalesOrder order = salesOrderClient.create(Map.of("accountId", "ACC-001"));
        assertThat(order.getUuid()).isEqualTo("so-uuid-1");

        wireMock.verify(postRequestedFor(urlPathEqualTo("/http/test/v2/sales-order/post")));
    }

    // ── cancel ──────────────────────────────────────────────────────────────

    @Test
    void cancelPostsToCorrectRoute() {
        wireMock.stubFor(post(urlPathEqualTo("/http/test/v1/sales-order/cancel"))
                .willReturn(okJson("{}")));

        assertThatCode(() -> salesOrderClient.cancel("SO-001"))
                .doesNotThrowAnyException();

        wireMock.verify(postRequestedFor(urlPathEqualTo("/http/test/v1/sales-order/cancel"))
                .withRequestBody(matchingJsonPath("$.id", equalTo("SO-001"))));
    }
}
