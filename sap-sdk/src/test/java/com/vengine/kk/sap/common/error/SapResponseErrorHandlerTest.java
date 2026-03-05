package com.vengine.kk.sap.common.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vengine.kk.sap.common.exception.AccountOrderBlockException;
import com.vengine.kk.sap.common.exception.SapClientException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

class SapResponseErrorHandlerTest {

    private SapResponseErrorHandler handler;

    @BeforeEach
    void setUp() {
        handler = new SapResponseErrorHandler(new ObjectMapper());
    }

    private MockClientHttpResponse response(String body) {
        return new MockClientHttpResponse(body.getBytes(StandardCharsets.UTF_8), HttpStatus.BAD_REQUEST);
    }

    @Test
    void severityCode3WithOrderBlockTypeIdThrowsAccountOrderBlockException() {
        String body = """
                {"Log": {"Item": [{"SeverityCode": "3", "TypeID": "034(/CL_CDA_BUSDT/)", "Note": "Order blocked"}]}}
                """;
        assertThatThrownBy(() -> handler.handleError(response(body)))
                .isInstanceOf(AccountOrderBlockException.class)
                .hasMessage("Account has order block");
    }

    @Test
    void severityCode3WithOtherTypeIdThrowsSapClientExceptionWithDetails() {
        String body = """
                {"Log": {"Item": [{"SeverityCode": "3", "TypeID": "099(/OTHER/)", "Note": "Something failed"}]}}
                """;
        assertThatThrownBy(() -> handler.handleError(response(body)))
                .isInstanceOf(SapClientException.class)
                .hasMessageContaining("099(/OTHER/)")
                .hasMessageContaining("Something failed");
    }

    @Test
    void severityCode2WarningOnlyNoException() {
        // Warning-only body — no severity 3 items
        String body = """
                {"Log": {"Item": [{"SeverityCode": "2", "TypeID": "WARN001", "Note": "Just a warning"}]}}
                """;
        // handleError should still throw a generic exception (it always throws at the end),
        // but it should NOT be an AccountOrderBlockException or contain TypeID info
        assertThatThrownBy(() -> handler.handleError(response(body)))
                .isInstanceOf(SapClientException.class)
                .satisfies(ex -> {
                    // Should be the generic fallback, not a matched error
                    assertThat(ex.getMessage()).contains("SAP HTTP error");
                });
    }

    @Test
    void singleLogItemAsObjectNotArrayHandledCorrectly() {
        String body = """
                {"Log": {"Item": {"SeverityCode": "3", "TypeID": "034(/CL_CDA_BUSDT/)", "Note": "Blocked"}}}
                """;
        assertThatThrownBy(() -> handler.handleError(response(body)))
                .isInstanceOf(AccountOrderBlockException.class);
    }

    @Test
    void missingLogItemNoExceptionFromLogParsing() {
        // No Log.Item at all — should fall through to generic exception
        String body = """
                {"Log": {}}
                """;
        assertThatThrownBy(() -> handler.handleError(response(body)))
                .isInstanceOf(SapClientException.class)
                .hasMessageContaining("SAP HTTP error");
    }

    @Test
    void severityCodeAsNumericIntegerHandled() {
        String body = """
                {"Log": {"Item": [{"SeverityCode": 3, "TypeID": "034(/CL_CDA_BUSDT/)", "Note": "Blocked"}]}}
                """;
        assertThatThrownBy(() -> handler.handleError(response(body)))
                .isInstanceOf(AccountOrderBlockException.class);
    }

    // OData format tests

    @Test
    void odataApCodeThrowsAccountOrderBlockException() {
        String body = """
                {"error": {"code": "AP001", "message": {"value": "Account has order block"}}}
                """;
        assertThatThrownBy(() -> handler.handleError(response(body)))
                .isInstanceOf(AccountOrderBlockException.class)
                .hasMessage("Account has order block");
    }

    @Test
    void odataSyCodeThrowsSapClientExceptionSystemError() {
        String body = """
                {"error": {"code": "SY123", "message": {"value": "System failure"}}}
                """;
        assertThatThrownBy(() -> handler.handleError(response(body)))
                .isInstanceOf(SapClientException.class)
                .isNotInstanceOf(AccountOrderBlockException.class)
                .hasMessage("SAP system error");
    }

    @Test
    void odataUnknownCodeThrowsSapClientExceptionWithMessage() {
        String body = """
                {"error": {"code": "XX999", "message": {"value": "Something went wrong"}}}
                """;
        assertThatThrownBy(() -> handler.handleError(response(body)))
                .isInstanceOf(SapClientException.class)
                .hasMessage("Something went wrong");
    }

    @Test
    void odataAndLogItemBothAbsentFallsToGeneric() {
        String body = """
                {"data": "no error here"}
                """;
        assertThatThrownBy(() -> handler.handleError(response(body)))
                .isInstanceOf(SapClientException.class)
                .hasMessageContaining("SAP HTTP error");
    }
}
