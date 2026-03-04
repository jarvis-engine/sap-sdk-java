package com.vengine.kk.sap.client.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    public static final String USAGE_REMITTANCE = "PAY001";
    public static final String USAGE_DUN_TO     = "PAY002";
    public static final String USAGE_SHIPPING   = "SHIP_TO";
    public static final String USAGE_BILLING    = "BILL_TO";
    public static final String USAGE_MAIN       = "XXDEFAULT";

    public static final int USAGE_CODE_NOT_USED = 0;
    public static final int USAGE_CODE_USED     = 1;
    public static final int USAGE_CODE_DEFAULT  = 2;

    @Nullable
    private String uuid;

    @Nullable
    private String country;

    @Nullable
    private String city;

    @Nullable
    private String street;

    @Nullable
    private String houseId;

    @Nullable
    private String postCode;

    @Nullable
    private String email;

    @Nullable
    private String phone;

    @Nullable
    private String title;

    @Builder.Default
    private List<Contact> contacts = new ArrayList<>();

    /**
     * Map of usage-code → { "default": boolean } entries, matching SAP semantics.
     */
    @Builder.Default
    private Map<String, Map<String, Object>> usage = java.util.Collections.emptyMap();

    @Nullable
    private String currencyCode;

    public boolean isMain() {
        return usage.containsKey(USAGE_MAIN);
    }

    public boolean isShipTo() {
        return usage.containsKey(USAGE_SHIPPING);
    }

    public boolean isBillTo() {
        return usage.containsKey(USAGE_BILLING);
    }

    public boolean isDunTo() {
        return usage.containsKey(USAGE_DUN_TO);
    }

    public boolean isRemittance() {
        return usage.containsKey(USAGE_REMITTANCE);
    }

    @Nullable
    public Contact getDefaultContact() {
        return contacts.stream()
                .filter(Contact::isDefaultContact)
                .findFirst()
                .orElse(null);
    }
}
