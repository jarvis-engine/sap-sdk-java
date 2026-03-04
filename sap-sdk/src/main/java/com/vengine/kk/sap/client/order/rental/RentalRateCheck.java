package com.vengine.kk.sap.client.order.rental;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a rental rate check request.
 * Maps from PHP {@code RentalRateCheckInput}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentalRateCheck {

    @Builder.Default
    private String calculationMode = "1";

    private String companyId;
    private boolean fixedReturnIndicator;
    private boolean planningPossible;
    private int quantity;
}
