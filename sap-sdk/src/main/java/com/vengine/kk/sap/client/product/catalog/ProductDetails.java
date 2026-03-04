package com.vengine.kk.sap.client.product.catalog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Detailed product attributes (shipping group, sortiment, availability scope, direct delivery).
 * Maps from PHP {@code ProductDetails}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetails {

    private String internalId;
    private String shippingGroup;

    /** Direct delivery indicator value, e.g. "1" (yes) or "2" (no). */
    private String directDelivery;

    private String sortiment;

    /** Availability check scope value from SAP. */
    private String availabilityCheckScope;
}
