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
public class PostalAddress {

    @Nullable
    private String country;

    @Nullable
    private String city;

    @Nullable
    private String street;

    @Nullable
    private String houseId;

    @Nullable
    private String postalCode;

    @Nullable
    private String email;
}
