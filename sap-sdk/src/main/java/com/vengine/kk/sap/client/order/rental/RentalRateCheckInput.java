package com.vengine.kk.sap.client.order.rental;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Input for checking rental rates in SAP ByDesign.
 *
 * <p>Mirrors PHP's {@code RentalRateCheckInput}.
 * Note: SAP payload field name is {@code companyID} (capital D) — handled in client.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentalRateCheckInput {

    @Builder.Default
    private String calculationMode = "1";

    /** Maps to SAP field {@code companyID} (capital D) */
    private String companyId;

    @Builder.Default
    private boolean fixedReturnIndicator = false;

    @Builder.Default
    private boolean planningPossible = true;

    private int quantity;
}
