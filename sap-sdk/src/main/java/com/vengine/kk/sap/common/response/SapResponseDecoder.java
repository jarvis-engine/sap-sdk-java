package com.vengine.kk.sap.common.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vengine.kk.sap.common.exception.SapClientException;

import java.util.ArrayList;
import java.util.List;

/**
 * Decodes SAP ByDesign OData-style JSON responses.
 *
 * <p>Single-entity responses use the {@code {"d": {...}}} envelope.
 * Collection responses use {@code {"d": {"results": [...]}}} — but SAP
 * sometimes returns a single object instead of an array in {@code results}
 * (the "ArrayConverter::toArrayList()" quirk). This decoder handles both cases.
 */
public class SapResponseDecoder {

    private final ObjectMapper objectMapper;

    public SapResponseDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Unwraps a single entity from the {@code {"d": {...}}} envelope.
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
     * Unwraps a collection from the {@code {"d": {"results": [...]}}} envelope.
     *
     * <p>Handles the SAP quirk where {@code results} may be a single JSON object
     * instead of an array — in that case the object is wrapped into a one-element list.
     */
    public <T> List<T> decodeList(String json, Class<T> itemType) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode resultsNode = root.path("d").path("results");

            if (resultsNode.isMissingNode()) {
                throw new SapClientException("Failed to decode SAP response: missing 'd.results' envelope");
            }

            List<T> items = new ArrayList<>();

            if (resultsNode.isArray()) {
                for (JsonNode element : resultsNode) {
                    items.add(objectMapper.treeToValue(element, itemType));
                }
            } else if (resultsNode.isObject()) {
                // SAP ArrayConverter quirk: single object instead of array
                items.add(objectMapper.treeToValue(resultsNode, itemType));
            } else {
                throw new SapClientException("Failed to decode SAP response: 'results' is neither array nor object");
            }

            return items;
        } catch (SapClientException e) {
            throw e;
        } catch (Exception e) {
            throw new SapClientException("Failed to decode SAP response: " + e.getMessage());
        }
    }
}
