package com.vengine.kk.sap.common.model;

import lombok.Data;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Common query parameters for SAP ByDesign API requests.
 * Mirrors PHP {@code Query} base class.
 */
@Data
public class SapQuery {

    @Nullable
    private String limit;

    @Nullable
    private String lastId;

    @Nullable
    private String countryCode;

    /**
     * Builds the query parameter map suitable for use as request params.
     */
    public Map<String, Object> toParamMap() {
        Map<String, Object> params = new HashMap<>();
        if (limit != null) {
            params.put("limit", limit);
        }
        if (lastId != null) {
            params.put("lastId", lastId);
        }
        if (countryCode != null) {
            params.put("countryCode", countryCode);
        }
        return params;
    }
}
