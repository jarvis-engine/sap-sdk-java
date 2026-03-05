package com.vengine.kk.sap.client.order.rental;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A party reference (account, billing, delivery) in a rental order.
 * Maps to SAP's {@code {"internalId": "...", "name": "..."}} party structure.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentalOrderParty {
    private String internalId;
    private String name;
}
