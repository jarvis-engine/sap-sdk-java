package com.vengine.kk.sap.client.account;

import com.vengine.kk.sap.client.account.request.CreateAccountRequest;
import com.vengine.kk.sap.client.account.request.UpdateAccountRequest;
import com.vengine.kk.sap.common.auth.SapAuthenticatedClientFactory;
import com.vengine.kk.sap.common.client.BaseSapClient;
import com.vengine.kk.sap.common.config.SapProperties;
import com.vengine.kk.sap.common.model.SapQuery;
import com.vengine.kk.sap.common.response.SapResponseDecoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Client for SAP ByDesign Customer/Account API.
 *
 * <p>Provides all 12 account operations: fetch, create, update, delete address/contact,
 * duplication check, and target-group lookup.
 *
 * <p>Route selection respects the {@code sap.features.customerV2EndpointEnabled} flag:
 * when enabled, fetch/create/update use the V2 endpoints.
 */
@Service
public class AccountClient extends BaseSapClient {

    // Route paths (without leading slash — buildUrl() adds the prefix)
    // V1 routes
    private static final String CUSTOMER_V1_FETCH          = "v1/customer/get";
    private static final String CUSTOMER_FETCH_ONE         = "v1/customer/get-one";
    private static final String CUSTOMER_CHECK_DUPLICATES  = "v1/customer/duplicate/get";
    private static final String CUSTOMER_TARGET_GROUP      = "v1/customer/target-group/get";
    private static final String CUSTOMER_DELETE_ADDRESS    = "v1/customer/address/delete";
    private static final String CUSTOMER_DELETE_CONTACT    = "v1/customer/relationship/delete";
    private static final String CUSTOMER_CREATE_ADDRESS    = "v1/customer/address/post";
    private static final String CUSTOMER_CREATE_CONTACT    = "v1/customer/relationship/post";

    // V2 routes
    private static final String CUSTOMER_V2_FETCH          = "v2/customer/get";
    private static final String CUSTOMER_CREATE            = "v2/customer/post";
    private static final String CUSTOMER_UPDATE            = "v2/customer/put";

    public AccountClient(SapAuthenticatedClientFactory factory,
                         SapProperties properties,
                         SapResponseDecoder decoder) {
        super(factory, properties, decoder);
    }

    // -------------------------------------------------------------------------
    // Fetch operations
    // -------------------------------------------------------------------------

    /**
     * Fetches a list of accounts with optional query parameters (limit, lastId, countryCode).
     * Uses V2 endpoint when {@code sap.features.customerV2EndpointEnabled} is true.
     */
    public List<Account> fetch(SapQuery query) {
        String base = properties.getFeatures().isCustomerV2EndpointEnabled()
                ? CUSTOMER_V2_FETCH : CUSTOMER_V1_FETCH;
        String route = appendQueryParams(base, query != null ? query.toParamMap() : java.util.Map.of());
        return getList(route, Account.class);
    }

    /**
     * Fetches a single account by its SAP UUID.
     */
    public Account fetchByUUID(String uuid) {
        String route = appendQueryParams(CUSTOMER_FETCH_ONE, Map.of("uuid", uuid));
        return get(route, Account.class);
    }

    /**
     * Fetches a single account by its internal SAP ID (e.g. "1000123").
     */
    public Account fetchById(String id) {
        String route = appendQueryParams(CUSTOMER_FETCH_ONE, Map.of("id", id));
        return get(route, Account.class);
    }

    /**
     * Fetches all accounts belonging to a target group.
     */
    public List<Account> fetchByTargetGroup(String targetGroupId) {
        String route = appendQueryParams(CUSTOMER_TARGET_GROUP, Map.of("targetGroupId", targetGroupId));
        return getList(route, Account.class);
    }

    /**
     * Checks for potential duplicate accounts based on name, street, city, and country.
     */
    public DuplicationResult checkDuplication(String name, String street, String city, String country) {
        String route = appendQueryParams(CUSTOMER_CHECK_DUPLICATES, Map.of(
                "name", name,
                "street", street,
                "city", city,
                "country", country
        ));
        return get(route, DuplicationResult.class);
    }

    /**
     * Fetches multiple accounts by a list of internal IDs.
     * Uses V2 endpoint when {@code sap.features.customerV2EndpointEnabled} is true.
     */
    public List<Account> fetchByIds(List<String> ids) {
        String base = properties.getFeatures().isCustomerV2EndpointEnabled()
                ? CUSTOMER_V2_FETCH : CUSTOMER_V1_FETCH;
        String route = appendQueryParams(base, Map.of("ids", String.join(",", ids)));
        return getList(route, Account.class);
    }

    // -------------------------------------------------------------------------
    // Mutation operations
    // -------------------------------------------------------------------------

    /**
     * Creates a new account (customer) in SAP.
     */
    public Account create(CreateAccountRequest request) {
        return post(CUSTOMER_CREATE, request, Account.class);
    }

    /**
     * Updates an existing account by UUID.
     */
    public Account update(String uuid, UpdateAccountRequest request) {
        String route = appendQueryParams(CUSTOMER_UPDATE, Map.of("uuid", uuid));
        return post(route, request, Account.class);
    }

    /**
     * Deletes an address from an account.
     */
    public void deleteAddress(String accountUuid, String addressUuid) {
        String route = appendQueryParams(CUSTOMER_DELETE_ADDRESS,
                Map.of("accountUuid", accountUuid, "addressUuid", addressUuid));
        delete(route);
    }

    /**
     * Deletes a contact (relationship) from an account.
     */
    public void deleteContact(String accountUuid, String contactUuid) {
        String route = appendQueryParams(CUSTOMER_DELETE_CONTACT,
                Map.of("accountUuid", accountUuid, "contactUuid", contactUuid));
        delete(route);
    }

    /**
     * Creates a new address for an existing account.
     */
    public void createAddress(String accountUuid, Address address) {
        String route = appendQueryParams(CUSTOMER_CREATE_ADDRESS, Map.of("accountUuid", accountUuid));
        postVoid(route, address);
    }

    /**
     * Creates a new contact (relationship) for an existing account.
     */
    public void createContact(String accountUuid, Contact contact) {
        String route = appendQueryParams(CUSTOMER_CREATE_CONTACT, Map.of("accountUuid", accountUuid));
        postVoid(route, contact);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

}
