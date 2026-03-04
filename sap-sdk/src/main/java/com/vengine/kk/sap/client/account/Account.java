package com.vengine.kk.sap.client.account;

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
public class Account {

    public static final String ACCOUNT_TYPE_PRIVATE   = "1";
    public static final String ACCOUNT_TYPE_CORPORATE = "2";

    public static final String INDUSTRY_ROAD_CONSTRUCTION       = "Z01";
    public static final String INDUSTRY_CIVIL_ENGINEERING       = "Z02";
    public static final String INDUSTRY_BUILDING_CONSTRUCTION   = "Z03";
    public static final String INDUSTRY_GARDENING_AND_LANDSCAPING = "Z04";
    public static final String INDUSTRY_INDUSTRY                = "Z05";

    public static final String LIFE_CYCLE_STATUS_IN_PREPARATION = "1";
    public static final String LIFE_CYCLE_STATUS_ACTIVE         = "2";
    public static final String LIFE_CYCLE_STATUS_BLOCKED        = "3";
    public static final String LIFE_CYCLE_STATUS_OBSOLETE       = "4";
    public static final String LIFE_CYCLE_STATUS_DELETED        = "5";

    public static final String DISTRIBUTION_ONLINE_SHOP = "Z1";
    public static final String DISTRIBUTION_DIRECT_SALES = "01";

    public static final String TAX_ID_CODE = "1";

    @Nullable
    private String uuid;

    @Nullable
    private String internalId;

    @Nullable
    private String changeStateId;

    @Nullable
    private String organizationName;

    @Nullable
    private String taxId;

    @Nullable
    private String classificationCode;

    @Nullable
    private String industry;

    @Builder.Default
    private List<String> channels = new ArrayList<>();

    @Builder.Default
    private List<Address> addresses = new ArrayList<>();

    @Nullable
    private OffsetDateTime createdAt;

    @Nullable
    private String crefoId;

    @Builder.Default
    private List<AssignedEmployee> assignedEmployees = new ArrayList<>();

    @Nullable
    private String lifeCycleStatusCode;

    @Nullable
    private String accountType;

    @Builder.Default
    private List<BusinessDocument> businessDocuments = new ArrayList<>();

    private boolean enabledForRent;

    @Nullable
    private String accountBlockReason;

    public boolean isInOnlineShop() {
        return channels.contains(DISTRIBUTION_ONLINE_SHOP);
    }

    public boolean isInDirectSales() {
        return channels.contains(DISTRIBUTION_DIRECT_SALES);
    }

    @Nullable
    public Address getMainAddress() {
        return addresses.stream()
                .filter(Address::isMain)
                .findFirst()
                .orElse(null);
    }

    @Nullable
    public Contact getMainContact() {
        for (Address address : addresses) {
            for (Contact contact : address.getContacts()) {
                if (contact.isDefaultContact()) {
                    return contact;
                }
            }
        }
        return null;
    }
}
