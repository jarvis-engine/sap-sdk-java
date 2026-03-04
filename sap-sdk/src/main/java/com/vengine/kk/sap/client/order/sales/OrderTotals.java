package com.vengine.kk.sap.client.order.sales;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderTotals {

    @Nullable
    private OrderPrice itemsNet;

    @Nullable
    private OrderPrice freightNet;

    @Nullable
    private OrderPrice itemsNetWithoutFreight;

    @Nullable
    private OrderPrice discountNet;

    @Nullable
    private OrderPrice overallDiscountByPercentageNet;

    @Nullable
    private OrderPrice additionalDiscountNet;

    @Nullable
    private OrderPrice additionalDiscountByPercentageNet;

    @Nullable
    private OrderTax taxGross;

    @Nullable
    private OrderPrice totalAmount;
}
