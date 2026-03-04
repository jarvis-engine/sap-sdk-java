package com.vengine.kk.sap.common.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vengine.kk.sap.common.exception.AccountOrderBlockException;
import com.vengine.kk.sap.common.exception.SapClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * HTTP error handler for SAP ByDesign responses.
 *
 * <p>On 4xx/5xx, reads the response body and inspects the SAP {@code Log.Item} array
 * for severity codes:
 * <ul>
 *   <li>3 = error → throw {@link SapClientException} (or a more specific subtype)</li>
 *   <li>2 = warning → log only</li>
 * </ul>
 *
 * <p>Maps from PHP {@code ResponseErrorHandler} and {@code SapExceptionHandler}.
 */
@Slf4j
public class SapResponseErrorHandler extends DefaultResponseErrorHandler {

    private static final int CODE_ERROR   = 3;
    private static final int CODE_WARNING = 2;

    private static final String TYPE_ID_ORDER_BLOCK = "034(/CL_CDA_BUSDT/)";

    private final ObjectMapper objectMapper;

    public SapResponseErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        byte[] bodyBytes = getResponseBody(response);
        String body = new String(bodyBytes, StandardCharsets.UTF_8);

        log.error("SAP API error — HTTP {}: {}", response.getStatusCode(), body);

        // Try to parse SAP Log envelope and match to specific exceptions
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> parsed = objectMapper.readValue(body, Map.class);
            checkForErrors(parsed, "HTTP " + response.getStatusCode());
        } catch (SapClientException e) {
            throw e;
        } catch (Exception e) {
            // Fall back to generic exception if body is not parseable JSON
            throw new SapClientException("SAP HTTP error " + response.getStatusCode() + ": " + body);
        }

        // Generic fallback if no errors found in log
        throw new SapClientException("SAP HTTP error " + response.getStatusCode() + ": " + body);
    }

    /**
     * Inspects the SAP {@code Log.Item} array in the response body for error entries.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void checkForErrors(Map<String, Object> contents, String operation) {
        Object log = contents.get("Log");
        if (!(log instanceof Map)) {
            return;
        }

        Object item = ((Map<?, ?>) log).get("Item");
        if (item == null) {
            return;
        }

        List<Map<String, Object>> logArray;
        if (item instanceof List) {
            logArray = (List<Map<String, Object>>) item;
        } else if (item instanceof Map) {
            logArray = Collections.singletonList((Map<String, Object>) item);
        } else {
            return;
        }

        handle(logArray, operation);
    }

    private void handle(List<Map<String, Object>> logArray, String operation) {
        for (Map<String, Object> record : logArray) {
            Object severityObj = record.get("SeverityCode");
            int severity = 0;
            if (severityObj instanceof Number) {
                severity = ((Number) severityObj).intValue();
            } else if (severityObj instanceof String) {
                try {
                    severity = Integer.parseInt((String) severityObj);
                } catch (NumberFormatException ignored) {
                }
            }

            if (CODE_WARNING == severity) {
                log.warn("{} SAP operation warning: {}", operation, record);
            }

            if (CODE_ERROR == severity) {
                log.error("{} SAP operation fail: {}", operation, record);
                throw matchErrorCodeToException(record);
            }
        }
    }

    private SapClientException matchErrorCodeToException(Map<String, Object> error) {
        String typeId = (String) error.getOrDefault("TypeID", "");
        String note   = (String) error.getOrDefault("Note",   "");

        if (TYPE_ID_ORDER_BLOCK.equals(typeId)) {
            return new AccountOrderBlockException("Account has order block");
        }

        return new SapClientException(
                String.format("Unknown SAP error code: %s, note: %s", typeId, note));
    }
}
