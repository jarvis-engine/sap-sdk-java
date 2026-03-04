package com.vengine.kk.sap.client.product.rental;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SerializedItem {

    private String internalUuid;
    private String articleNumber;
    private String serialNumber;
    private String costCenterId;
    private boolean enabled;
}
