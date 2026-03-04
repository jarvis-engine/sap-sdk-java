package com.vengine.kk.sap.common.error;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vengine.kk.sap.common.exception.AccountOrderBlockException;
import com.vengine.kk.sap.common.exception.SapClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Parses SAP OData error responses and throws typed exceptions.
 *
 * <p>SAP error body shape:
 * <pre>{@code {"error": {"code": "...", "message": {"value": "..."}}}}</pre>
 *
 * <p>Error-code routing (from PHP SDK):
 * <ul>
 *   <li>{@code "AP"} prefix → {@link AccountOrderBlockException}</li>
 *   <li>{@code "SY"} prefix → {@link SapClientException} with "SAP system error"</li>
 *   <li>anything else → {@link SapClientException} with the raw message value</li>
 * </ul>
 */
@Slf4j
@Component
public class SapExceptionHandler {

    private final ObjectMapper objectMapper;

    public SapExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Parses the SAP error body and throws a typed exception.
     *
     * @param responseBody the raw JSON response body
     * @param httpStatus   the HTTP status code
     * @throws SapClientException always
     */
    public void handle(String responseBody, int httpStatus) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode errorNode = root.path("error");

            if (errorNode.isMissingNode()) {
                throw new SapClientException("SAP error (HTTP " + httpStatus + ")");
            }

            String code = errorNode.path("code").asText("");
            String message = errorNode.path("message").path("value").asText("");

            log.error("SAP error — HTTP {} code={} message={}", httpStatus, code, message);

            if (code.startsWith("AP")) {
                throw new AccountOrderBlockException(message);
            }
            if (code.startsWith("SY")) {
                throw new SapClientException("SAP system error");
            }

            throw new SapClientException(message);
        } catch (SapClientException e) {
            // covers AccountOrderBlockException (subclass)
            throw e;
        } catch (Exception e) {
            throw new SapClientException("SAP error (HTTP " + httpStatus + ")");
        }
    }
}
