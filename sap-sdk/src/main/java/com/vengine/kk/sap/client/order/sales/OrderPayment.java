package com.vengine.kk.sap.client.order.sales;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPayment {

    private String provider;

    @Nullable
    private String paymentReference;

    private String paidAmount;
    private String paidAmountCurrency;
    private OffsetDateTime transactionDate;
}
