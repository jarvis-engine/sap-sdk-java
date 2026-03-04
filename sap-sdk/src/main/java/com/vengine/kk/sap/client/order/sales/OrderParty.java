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
public class OrderParty {

    @Nullable
    private String partyId;

    @Nullable
    private String firstLineName;

    @Nullable
    private String secondLineName;

    @Nullable
    private OrderContact contact;

    @Nullable
    private PostalAddress postalAddress;

    @Nullable
    private OrderParty contactParty;
}
