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
public class CustomerIdentifiers {

    @Nullable
    private String email;

    @Nullable
    private String taxId;

    @Nullable
    private String crefoId;

    @Nullable
    private String objectId;

    @Nullable
    private String name;

    @Nullable
    private String surname;

    @Nullable
    private String contactEmail;
}
