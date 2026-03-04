package com.vengine.kk.sap.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Pagination metadata returned in SAP collection responses.
 * Maps from SAP fields: ReturnedQueryHitsNumberValue, MoreHitsAvailableIndicator, LastReturnedObjectID
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageMetadata {

    private boolean nextAvailable;
    private String lastObjectId;
    private int count;
}
