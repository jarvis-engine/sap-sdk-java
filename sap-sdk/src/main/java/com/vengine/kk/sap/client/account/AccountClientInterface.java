package com.vengine.kk.sap.client.account;

import com.vengine.kk.sap.client.account.request.CreateAccountRequest;
import com.vengine.kk.sap.client.account.request.UpdateAccountRequest;
import com.vengine.kk.sap.common.model.SapQuery;

import java.util.List;

/**
 * Interface for SAP ByDesign Account/Customer operations.
 * Implement this interface in tests to mock SAP account calls.
 */
public interface AccountClientInterface {

    List<Account> fetch(SapQuery query);

    Account fetchByUUID(String uuid);

    Account fetchById(String id);

    List<Account> fetchByTargetGroup(String targetGroupId);

    DuplicationResult checkDuplication(String name, String street, String city, String country);

    List<Account> fetchByIds(List<String> ids);

    Account create(CreateAccountRequest request);

    Account update(String uuid, UpdateAccountRequest request);

    void deleteAddress(String accountUuid, String addressUuid);

    void deleteContact(String accountUuid, String contactUuid);

    void createAddress(String accountUuid, Address address);

    void createContact(String accountUuid, Contact contact);
}
