package com.vengine.kk.sap.common.error;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vengine.kk.sap.common.exception.AccountOrderBlockException;
import com.vengine.kk.sap.common.exception.SapClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class SapResponseErrorHandler extends DefaultResponseErrorHandler {

    private static final String ORDER_BLOCK_TYPE_ID = "034(/CL_CDA_BUSDT/)";
    private static final int SEVERITY_ERROR   = 3;
    private static final int SEVERITY_WARNING = 2;

    private final ObjectMapper objectMapper;

    public SapResponseErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        byte[] bodyBytes = getResponseBody(response);
        String body = new String(bodyBytes, StandardCharsets.UTF_8);
        int status = response.getStatusCode().value();

        try {
            JsonNode root = objectMapper.readTree(body);

            // 1. OData error format: {"error": {"code": "...", "message": {"value": "..."}}}
            JsonNode errorNode = root.get("error");
            if (errorNode != null && !errorNode.isNull()) {
                String code    = errorNode.path("code").asText("");
                String message = errorNode.path("message").path("value").asText("");
                if (!message.isBlank() || !code.isBlank()) {
                    if (code.startsWith("AP")) {
                        log.warn("SAP account order block — code: {}, message: {}", code, message);
                        throw new AccountOrderBlockException(message);
                    }
                    if (code.startsWith("SY")) {
                        log.error("SAP system error — code: {}, HTTP {}", code, status);
                        throw new SapClientException("SAP system error");
                    }
                    String msg = message.isBlank() ? "SAP error (HTTP " + status + ")" : message;
                    log.warn("SAP business error — code: {}, message: {}", code, msg);
                    throw new SapClientException(msg);
                }
            }

            // 2. Log.Item format: {"Log": {"Item": [...]}} or {"Log": {"Item": {...}}}
            JsonNode logNode = root.path("Log").path("Item");
            if (!logNode.isMissingNode() && !logNode.isNull()) {
                if (logNode.isArray()) {
                    for (JsonNode item : logNode) processLogItem(item);
                } else if (logNode.isObject()) {
                    processLogItem(logNode);
                }
            }

        } catch (SapClientException e) {
            throw e;
        } catch (Exception ignored) {
            // body not parseable JSON — fall through to generic
        }

        // Generic fallback — truly unexpected
        log.error("SAP HTTP error {} — unparseable response: {}", status, body);
        throw new SapClientException("SAP HTTP error " + status);
    }

    private void processLogItem(JsonNode item) {
        int severityCode = item.path("SeverityCode").asInt(0);
        if (severityCode == SEVERITY_WARNING) {
            log.warn("SAP Log.Item warning: {}", item);
        }
        if (severityCode == SEVERITY_ERROR) {
            String typeId = item.path("TypeID").asText("");
            String note   = item.path("Note").asText("");
            if (ORDER_BLOCK_TYPE_ID.equals(typeId)) {
                log.warn("SAP account order block — typeId: {}, note: {}", typeId, note);
                throw new AccountOrderBlockException("Account has order block");
            }
            log.warn("SAP Log.Item error — typeId: {}, note: {}", typeId, note);
            throw new SapClientException("Unknown SAP error code: " + typeId + ", note: " + note);
        }
    }
}
