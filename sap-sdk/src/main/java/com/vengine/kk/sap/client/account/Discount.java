package com.vengine.kk.sap.client.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Discount {

    private static final int STATUS_ACTIVE = 3;

    private String code;
    private String name;
    private double percentMultiplier;
    private int statusCode;

    public boolean isActive() {
        return STATUS_ACTIVE == statusCode;
    }
}
