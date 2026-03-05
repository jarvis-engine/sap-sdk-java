package com.vengine.kk.sap.common.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vengine.kk.sap.common.exception.AccountOrderBlockException;
import com.vengine.kk.sap.common.exception.SapClientException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class SapExceptionHandlerTest {

    private SapExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new SapExceptionHandler(new ObjectMapper());
    }

    @Test
    void apCodeThrowsAccountOrderBlockException() {
        String body = """
                {"error": {"code": "AP001", "message": {"value": "Account has order block"}}}
                """;
        assertThatThrownBy(() -> handler.handle(body, 400))
                .isInstanceOf(AccountOrderBlockException.class)
                .hasMessage("Account has order block");
    }

    @Test
    void syCodeThrowsSapClientExceptionWithSystemError() {
        String body = """
                {"error": {"code": "SY123", "message": {"value": "Some system failure"}}}
                """;
        assertThatThrownBy(() -> handler.handle(body, 500))
                .isInstanceOf(SapClientException.class)
                .isNotInstanceOf(AccountOrderBlockException.class)
                .hasMessage("SAP system error");
    }

    @Test
    void unknownCodeThrowsSapClientExceptionWithRawMessage() {
        String body = """
                {"error": {"code": "XX999", "message": {"value": "Something went wrong"}}}
                """;
        assertThatThrownBy(() -> handler.handle(body, 422))
                .isInstanceOf(SapClientException.class)
                .hasMessage("Something went wrong");
    }

    @Test
    void missingErrorNodeThrowsSapClientExceptionWithHttpStatus() {
        String body = """
                {"data": "no error node here"}
                """;
        assertThatThrownBy(() -> handler.handle(body, 422))
                .isInstanceOf(SapClientException.class)
                .hasMessage("SAP error (HTTP 422)");
    }

    @Test
    void malformedJsonThrowsSapClientException() {
        assertThatThrownBy(() -> handler.handle("{not json", 500))
                .isInstanceOf(SapClientException.class)
                .hasMessage("SAP error (HTTP 500)");
    }
}
