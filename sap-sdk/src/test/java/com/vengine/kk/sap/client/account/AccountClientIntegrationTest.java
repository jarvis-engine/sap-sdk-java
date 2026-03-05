package com.vengine.kk.sap.client.account;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.maciejwalkowiak.wiremock.spring.ConfigureWireMock;
import com.maciejwalkowiak.wiremock.spring.EnableWireMock;
import com.maciejwalkowiak.wiremock.spring.InjectWireMock;
import com.vengine.kk.sap.client.account.request.CreateAccountRequest;
import com.vengine.kk.sap.common.exception.AccountOrderBlockException;
import com.vengine.kk.sap.common.exception.SapClientException;
import com.vengine.kk.sap.common.model.SapQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@EnableWireMock({
        @ConfigureWireMock(name = "sap", property = "sap.base-url")
})
class AccountClientIntegrationTest {

    @InjectWireMock("sap")
    private WireMockServer wireMock;

    @Autowired
    private AccountClient accountClient;

    @BeforeEach
    void resetStubs() {
        wireMock.resetAll();
    }

    // ── fetch V1 ────────────────────────────────────────────────────────────

    @Test
    void fetchReturnsAccountListV1() {
        wireMock.stubFor(get(urlPathEqualTo("/http/test/v1/customer/get"))
                .willReturn(okJson("""
                        {"d": {"results": [
                            {"uuid": "aaa-111", "internalId": "1001", "organizationName": "Alpha GmbH"},
                            {"uuid": "bbb-222", "internalId": "1002", "organizationName": "Beta AG"}
                        ]}}
                        """)));

        SapQuery query = new SapQuery();
        query.setLimit("10");
        List<Account> accounts = accountClient.fetch(query);

        assertThat(accounts).hasSize(2);
        assertThat(accounts.get(0).getUuid()).isEqualTo("aaa-111");
        assertThat(accounts.get(1).getOrganizationName()).isEqualTo("Beta AG");
    }

    // ── fetchByUUID ─────────────────────────────────────────────────────────

    @Test
    void fetchByUuidHitsCorrectEndpoint() {
        wireMock.stubFor(get(urlPathEqualTo("/http/test/v1/customer/get-one"))
                .withQueryParam("uuid", equalTo("uuid-123"))
                .willReturn(okJson("""
                        {"d": {"uuid": "uuid-123", "internalId": "1001", "organizationName": "Test GmbH"}}
                        """)));

        Account account = accountClient.fetchByUUID("uuid-123");
        assertThat(account.getUuid()).isEqualTo("uuid-123");
        assertThat(account.getOrganizationName()).isEqualTo("Test GmbH");
    }

    // ── fetchById ───────────────────────────────────────────────────────────

    @Test
    void fetchByIdHitsCorrectEndpoint() {
        wireMock.stubFor(get(urlPathEqualTo("/http/test/v1/customer/get-one"))
                .withQueryParam("id", equalTo("1001"))
                .willReturn(okJson("""
                        {"d": {"uuid": "uuid-abc", "internalId": "1001", "organizationName": "Test GmbH"}}
                        """)));

        Account account = accountClient.fetchById("1001");
        assertThat(account.getInternalId()).isEqualTo("1001");
    }

    // ── checkDuplication ────────────────────────────────────────────────────

    @Test
    void checkDuplicationSendsCorrectQueryParams() {
        wireMock.stubFor(get(urlPathEqualTo("/http/test/v1/customer/duplicate/get"))
                .withQueryParam("name", equalTo("Test GmbH"))
                .withQueryParam("street", equalTo("Main St"))
                .withQueryParam("city", equalTo("Berlin"))
                .withQueryParam("country", equalTo("DE"))
                .willReturn(okJson("""
                        {"d": {"duplicateEmail": true, "duplicateTaxId": false, "duplicateCrefoId": false, "duplicateContactPerson": false}}
                        """)));

        DuplicationResult result = accountClient.checkDuplication("Test GmbH", "Main St", "Berlin", "DE");
        assertThat(result.isDuplicateEmail()).isTrue();
        assertThat(result.isDuplicateTaxId()).isFalse();
    }

    // ── create ──────────────────────────────────────────────────────────────

    @Test
    void createSendsPostAndReturnsAccount() {
        wireMock.stubFor(post(urlPathEqualTo("/http/test/v2/customer/post"))
                .willReturn(okJson("""
                        {"d": {"uuid": "new-uuid", "internalId": "1003", "organizationName": "New Corp"}}
                        """)));

        CreateAccountRequest request = CreateAccountRequest.builder()
                .account(Account.builder().organizationName("New Corp").build())
                .build();

        Account created = accountClient.create(request);
        assertThat(created.getUuid()).isEqualTo("new-uuid");
        assertThat(created.getOrganizationName()).isEqualTo("New Corp");
    }

    // ── deleteAddress ───────────────────────────────────────────────────────

    @Test
    void deleteAddressSendsDeleteToCorrectUrl() {
        wireMock.stubFor(delete(urlPathEqualTo("/http/test/v1/customer/address/delete"))
                .withQueryParam("accountUuid", equalTo("acc-uuid"))
                .withQueryParam("addressUuid", equalTo("addr-uuid"))
                .willReturn(ok()));

        assertThatCode(() -> accountClient.deleteAddress("acc-uuid", "addr-uuid"))
                .doesNotThrowAnyException();

        wireMock.verify(deleteRequestedFor(urlPathEqualTo("/http/test/v1/customer/address/delete"))
                .withQueryParam("accountUuid", equalTo("acc-uuid"))
                .withQueryParam("addressUuid", equalTo("addr-uuid")));
    }

    // ── Error handling ──────────────────────────────────────────────────────

    @Test
    void sapApErrorViaLogItemThrowsAccountOrderBlockException() {
        wireMock.stubFor(get(urlPathEqualTo("/http/test/v1/customer/get-one"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"Log": {"Item": [{"SeverityCode": "3", "TypeID": "034(/CL_CDA_BUSDT/)", "Note": "Account has order block"}]}}
                                """)));

        assertThatThrownBy(() -> accountClient.fetchByUUID("uuid-blocked"))
                .isInstanceOf(AccountOrderBlockException.class)
                .hasMessageContaining("Account has order block");
    }

    @Test
    void sapErrorWithUnknownTypeIdThrowsSapClientException() {
        wireMock.stubFor(get(urlPathEqualTo("/http/test/v1/customer/get-one"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"Log": {"Item": [{"SeverityCode": "3", "TypeID": "SY001", "Note": "System failure"}]}}
                                """)));

        assertThatThrownBy(() -> accountClient.fetchByUUID("uuid-sys"))
                .isInstanceOf(SapClientException.class)
                .hasMessageContaining("SY001");
    }

    @Test
    void sapApErrorViaOdataFormatThrowsAccountOrderBlockException() {
        wireMock.stubFor(get(urlPathEqualTo("/http/test/v1/customer/get-one"))
                .withQueryParam("uuid", equalTo("uuid-odata-blocked"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"error": {"code": "AP002", "message": {"value": "Customer order block active"}}}
                                """)));

        assertThatThrownBy(() -> accountClient.fetchByUUID("uuid-odata-blocked"))
                .isInstanceOf(AccountOrderBlockException.class)
                .hasMessage("Customer order block active");
    }

    @Test
    void http500WithGenericBodyThrowsSapClientException() {
        wireMock.stubFor(get(urlPathEqualTo("/http/test/v1/customer/get-one"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"message": "Internal Server Error"}
                                """)));

        assertThatThrownBy(() -> accountClient.fetchByUUID("uuid-500"))
                .isInstanceOf(SapClientException.class);
    }
}
