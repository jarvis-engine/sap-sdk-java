package com.vengine.kk.sap.client.order.rental;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a rental price check request/response.
 * Maps from PHP {@code RentalPriceCheckInput} and related product result models.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentalPriceCheck {

    @Builder.Default
    private String currency = "EUR";

    private OffsetDateTime pricingDate;
    private String distributionChannel;

    @Nullable
    private String accountId;

    private String salesUnitId;
    private String sellerParty;

    @Nullable
    private String pricingModel;

    @Builder.Default
    private List<RentalPriceCheckProduct> products = new ArrayList<>();
}
