package com.vengine.kk.sap.common.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vengine.kk.sap.common.exception.SapClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Decodes SAP ByDesign JSON responses.
 *
 * <p>SAP ByDesign uses two distinct response envelope formats:
 *
 * <p><b>OData format</b> (product catalog, account, employee endpoints):
 * <pre>{"d": {...}}  or  {"d": {"results": [...]}}</pre>
 *
 * <p><b>Custom SOAP/REST envelope format</b> (rental, service order endpoints):
 * <pre>{"n0:ServiceOrderByElementsResponse_synC": {"ServiceOrder": {...}}}</pre>
 * The outer key varies per endpoint. The decoder unwraps it automatically,
 * then the caller specifies a {@code nodeKey} to access the target payload.
 *
 * <p>This mirrors the logic of PHP's {@code ResponseContentsDecoder}.
 */
public class SapResponseDecoder {

    /**
     * Operations that must be unwrapped one level before accessing the nodeKey.
     * Mirrors PHP's IGNORED_COLLECTIONS + the _sync/_Collection unwrap logic.
     */
    private static final Set<String> ALWAYS_UNWRAP = Set.of(
        "KKDigital_GetCustomers_DirectSales",
        "KKDigital_GetCustomers_OnlineSales",
        "KKDigital_GetCustomers",
        "SalesOrderHistory",
        "SalesOrdersShippingConditions",
        "n0:ServiceOrderByElementsResponse_synC",   // rental order create (note capital C — SAP typo)
        "n0:RentalRateCalculatorReadByIDResponse_sync"  // rental rate check
    );

    private final ObjectMapper objectMapper;

    public SapResponseDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // OData-style decode (d / d.results envelope) — used by most GET endpoints
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Unwraps a single entity from the {@code {"d": {...}}} OData envelope.
     */
    public <T> T decode(String json, Class<T> type) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode dNode = root.path("d");
            if (dNode.isMissingNode()) {
                throw new SapClientException("Failed to decode SAP response: missing 'd' envelope");
            }
            return objectMapper.treeToValue(dNode, type);
        } catch (SapClientException e) {
            throw e;
        } catch (Exception e) {
            throw new SapClientException("Failed to decode SAP response: " + e.getMessage());
        }
    }

    /**
     * Unwraps a collection from the {@code {"d": {"results": [...]}}} OData envelope.
     *
     * <p>Handles the SAP quirk where {@code results} may be a single JSON object
     * instead of an array — wraps it into a one-element list.
     */
    public <T> List<T> decodeList(String json, Class<T> itemType) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode resultsNode = root.path("d").path("results");

            if (resultsNode.isMissingNode()) {
                throw new SapClientException("Failed to decode SAP response: missing 'd.results' envelope");
            }

            return collectNodes(resultsNode, itemType);
        } catch (SapClientException e) {
            throw e;
        } catch (Exception e) {
            throw new SapClientException("Failed to decode SAP response: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SAP envelope decode (n0:*_sync / FixedAsset / etc.) — used by rental/service endpoints
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Decodes a single entity using SAP's custom envelope format.
     *
     * <p>Unwraps the outer operation envelope (e.g. {@code n0:ServiceOrderByElementsResponse_synC})
     * then accesses {@code contents[nodeKey]} to find the target object.
     *
     * @param json    raw SAP JSON response
     * @param nodeKey the key inside the (unwrapped) response that holds the target entity
     * @param type    target Java type
     */
    public <T> T decodeWithNode(String json, String nodeKey, Class<T> type) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode contents = unwrapEnvelope(root);
            JsonNode target = contents.path(nodeKey);
            if (target.isMissingNode()) {
                throw new SapClientException(
                    "SAP response missing expected node '" + nodeKey + "'. " +
                    "Available keys: " + fieldNames(contents));
            }
            return objectMapper.treeToValue(target, type);
        } catch (SapClientException e) {
            throw e;
        } catch (Exception e) {
            throw new SapClientException("Failed to decode SAP response (node=" + nodeKey + "): " + e.getMessage());
        }
    }

    /**
     * Decodes a list using SAP's custom envelope format.
     *
     * <p>Unwraps the outer envelope then accesses {@code contents[nodeKey]} as an array.
     * Handles the SAP quirk where a single-item list is returned as a bare object.
     */
    public <T> List<T> decodeListWithNode(String json, String nodeKey, Class<T> itemType) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode contents = unwrapEnvelope(root);
            JsonNode target = contents.path(nodeKey);
            if (target.isMissingNode()) {
                throw new SapClientException(
                    "SAP response missing expected node '" + nodeKey + "'. " +
                    "Available keys: " + fieldNames(contents));
            }
            return collectNodes(target, itemType);
        } catch (SapClientException e) {
            throw e;
        } catch (Exception e) {
            throw new SapClientException("Failed to decode SAP list (node=" + nodeKey + "): " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Raw JsonNode access — for complex nested parsing (e.g. price components)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the raw {@link JsonNode} array at {@code contents[nodeKey]}.
     * Use this when the response structure is too complex for automatic deserialization
     * (e.g. nested PriceComponents that need manual traversal).
     */
    public List<JsonNode> decodeRawListWithNode(String json, String nodeKey) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode contents = unwrapEnvelope(root);
            JsonNode target = contents.path(nodeKey);
            if (target.isMissingNode()) {
                throw new SapClientException(
                    "SAP response missing expected node '" + nodeKey + "'. " +
                    "Available keys: " + fieldNames(contents));
            }
            List<JsonNode> items = new ArrayList<>();
            if (target.isArray()) {
                target.forEach(items::add);
            } else if (target.isObject()) {
                items.add(target); // single-item quirk
            }
            return items;
        } catch (SapClientException e) {
            throw e;
        } catch (Exception e) {
            throw new SapClientException("Failed to decode raw SAP list (node=" + nodeKey + "): " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Internal helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Mirrors PHP's ResponseContentsDecoder envelope-unwrap logic.
     *
     * <p>If the first key of the response contains {@code _sync}, {@code Collection},
     * {@code KKDigital_GetEmployees}, equals {@code n0:SalesOrderByElementsResponse_sync},
     * or is in {@link #ALWAYS_UNWRAP} — unwrap one level. Otherwise return root as-is.
     */
    private JsonNode unwrapEnvelope(JsonNode root) {
        if (!root.isObject() || !root.fieldNames().hasNext()) {
            return root;
        }
        String operation = root.fieldNames().next();
        if (shouldUnwrap(operation)) {
            return root.path(operation);
        }
        return root;
    }

    private boolean shouldUnwrap(String operation) {
        return operation.contains("_sync")
            || operation.contains("Collection")
            || operation.contains("KKDigital_GetEmployees")
            || "n0:SalesOrderByElementsResponse_sync".equals(operation)
            || ALWAYS_UNWRAP.contains(operation);
    }

    private <T> List<T> collectNodes(JsonNode node, Class<T> itemType) throws Exception {
        List<T> items = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode element : node) {
                items.add(objectMapper.treeToValue(element, itemType));
            }
        } else if (node.isObject()) {
            items.add(objectMapper.treeToValue(node, itemType));
        } else {
            throw new SapClientException("SAP response node is neither array nor object");
        }
        return items;
    }

    private String fieldNames(JsonNode node) {
        List<String> names = new ArrayList<>();
        node.fieldNames().forEachRemaining(names::add);
        return names.toString();
    }
}
