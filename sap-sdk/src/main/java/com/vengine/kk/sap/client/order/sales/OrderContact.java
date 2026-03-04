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
public class OrderContact {

    @Nullable
    private String email;

    @Nullable
    private String phoneNumber;

    @Nullable
    private String mobileNumber;

    @Nullable
    private String firstName;

    @Nullable
    private String lastName;

    @Nullable
    private String uuid;

    @Nullable
    private String internalId;

    @Nullable
    private String salutation;

    @Nullable
    private String fullName;
}
