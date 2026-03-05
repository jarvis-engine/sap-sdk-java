package com.vengine.kk.sap.client.account;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.maciejwalkowiak.wiremock.spring.ConfigureWireMock;
import com.maciejwalkowiak.wiremock.spring.EnableWireMock;
import com.maciejwalkowiak.wiremock.spring.InjectWireMock;
import com.vengine.kk.sap.common.model.SapQuery;
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
@TestPropertySource(properties = "sap.features.customer-v2-endpoint-enabled=true")
class AccountClientV2IntegrationTest {

    @InjectWireMock("sap")
    private WireMockServer wireMock;

    @Autowired
    private AccountClient accountClient;

    @BeforeEach
    void resetStubs() {
        wireMock.resetAll();
    }

    @Test
    void fetchHitsV2EndpointWhenFlagEnabled() {
        wireMock.stubFor(get(urlPathEqualTo("/http/test/v2/customer/get"))
                .willReturn(okJson("""
                        {"d": {"results": [
                            {"uuid": "v2-uuid", "internalId": "2001", "organizationName": "V2 Corp"}
                        ]}}
                        """)));

        SapQuery query = new SapQuery();
        query.setLimit("5");
        List<Account> accounts = accountClient.fetch(query);

        assertThat(accounts).hasSize(1);
        assertThat(accounts.get(0).getOrganizationName()).isEqualTo("V2 Corp");

        wireMock.verify(getRequestedFor(urlPathEqualTo("/http/test/v2/customer/get")));
    }
}
