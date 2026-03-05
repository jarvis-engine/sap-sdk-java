package com.vengine.kk.sap.client.order.rental;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response from creating a rental order in SAP ByDesign.
 *
 * <p>SAP envelope: {@code n0:ServiceOrderByElementsResponse_synC} → {@code ServiceOrder}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentalOrder {

    @JsonAlias("ID")
    private String internalId;

    @JsonAlias("UUID")
    private String uuid;
}
