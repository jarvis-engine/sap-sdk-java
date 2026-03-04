package com.vengine.kk.sap.client.order.sales;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrder {

    public static final String E_SHOP          = "E-Shop";
    public static final String EMPLOYEE_PORTAL = "Employee-Portal";

    private String internalId;
    private String uuid;
    private String status;
    private OffsetDateTime createdAt;
    private OrderTotals totals;
    private int creditWorthinessStatus;

    @Nullable
    private String paymentOption;

    @Nullable
    private OrderParty accountParty;

    @Nullable
    private OrderParty billingParty;

    @Nullable
    private OrderParty deliveryParty;

    @Nullable
    private OrderParty employeeParty;

    @Nullable
    private String salesUnitId;

    @Builder.Default
    private List<OrderProduct> products = new ArrayList<>();

    @Nullable
    private String shippingCondition;

    @Nullable
    private String clientExternalReference;

    @Nullable
    private String system;

    @Nullable
    private String distributionChannel;
}
