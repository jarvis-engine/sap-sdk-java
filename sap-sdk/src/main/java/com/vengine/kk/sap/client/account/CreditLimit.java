package com.vengine.kk.sap.client.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditLimit {

    private String internalId;

    @Nullable
    private String creditLimitAmount;

    @Nullable
    private String creditLimitCurrency;

    @Nullable
    private String remainingCreditLimitAmount;

    @Nullable
    private String remainingCreditLimitCurrency;

    @Nullable
    private String creditWorthinessStatus;
}
