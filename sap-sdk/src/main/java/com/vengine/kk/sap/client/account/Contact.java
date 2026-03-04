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
public class Contact {

    public static final int GENDER_UNKNOWN = 0;
    public static final int GENDER_MALE    = 1;
    public static final int GENDER_FEMALE  = 2;

    private static final String SALUTATION_MALE   = "0002";
    private static final String SALUTATION_FEMALE = "0001";

    @Nullable
    private String uuid;

    @Nullable
    private String internalId;

    @Builder.Default
    private boolean defaultContact = true;

    @Nullable
    private String addressUuid;

    @Builder.Default
    private int gender = GENDER_UNKNOWN;

    @Nullable
    private String givenName;

    @Nullable
    private String familyName;

    @Nullable
    private String email;

    @Nullable
    private String phone;

    @Nullable
    private String mobile;

    public String getSalutationCode() {
        return switch (gender) {
            case GENDER_FEMALE -> SALUTATION_FEMALE;
            case GENDER_MALE   -> SALUTATION_MALE;
            default            -> "";
        };
    }
}
