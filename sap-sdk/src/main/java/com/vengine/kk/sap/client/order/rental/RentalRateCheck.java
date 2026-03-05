package com.vengine.kk.sap.client.order.rental;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response from a rental rate check in SAP ByDesign.
 *
 * <p>SAP envelope: {@code n0:RentalRateCalculatorReadByIDResponse_sync} → {@code RentalRateCalculator}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentalRateCheck {

    @JsonAlias("SAP_UUID")
    private String uuid;

    @JsonAlias("Quantity")
    private int quantity;

    @JsonAlias("RentalRate")
    private String rentalRate;

    @JsonAlias("RentalRateName")
    private String rentalRateName;
}
