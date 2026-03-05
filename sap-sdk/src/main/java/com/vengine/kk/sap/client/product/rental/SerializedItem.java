package com.vengine.kk.sap.client.product.rental;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * A physical rental machine tracked by serial number in SAP ByDesign.
 *
 * <p>SAP returns items under the {@code FixedAsset} node.
 * {@code @JsonAlias} accepts SAP field names during deserialization;
 * the REST response uses clean Java field names.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SerializedItem {

    @JsonAlias("UUID")
    private String internalUuid;

    @JsonAlias("VVS_UnitType")
    private String articleNumber;

    @JsonAlias("VVS_UnitNumber")
    private String serialNumber;

    @JsonAlias("CostCentreID")
    private String costCenterId;

    /** Raw SAP field: string "true" = available. Use {@link #isEnabled()} for boolean. */
    @JsonAlias("VVS_EnabledForFieldVu")
    private String enabledForFieldVu;

    /** {@code true} if machine is available for rental. */
    @JsonProperty("enabled")
    public boolean isEnabled() {
        return "true".equals(enabledForFieldVu);
    }
}
