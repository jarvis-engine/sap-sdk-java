package com.vengine.kk.sap.client.order.rental;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * A product line item within a {@link RentalOrderInput}.
 * Mirrors PHP's {@code RentalOrderProduct} input model.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentalOrderProductInput {

    private String internalId;
    private int quantity;
    private OffsetDateTime startDateTime;
    private OffsetDateTime endDateTime;
    private String discount;
    private String productPrice;
}
