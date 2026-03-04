package com.vengine.kk.sap.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic SAP ByDesign response envelope.
 *
 * @param <T> the type of the payload items
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SapResponse<T> {

    private List<T> items;
    private PageMetadata metadata;
}
