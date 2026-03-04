package com.vengine.kk.sap.client.delivery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryCost {

    private String uuid;

    private String freightCostAmount;
    private String freightCostCurrencyCode;

    private String carriagePaidFromThresholdAmount;
    private String carriagePaidFromCurrencyCode;

    private String countryCode;
    private String shippingCostGroupCode;

    @Nullable
    private String postCode;

    private boolean defaultShippingCostIndicator;
}
