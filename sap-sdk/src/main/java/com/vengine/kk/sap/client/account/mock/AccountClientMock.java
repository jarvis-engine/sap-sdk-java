package com.vengine.kk.sap.client.account.mock;

import com.vengine.kk.sap.client.account.Account;
import com.vengine.kk.sap.client.account.AccountClientInterface;
import com.vengine.kk.sap.client.account.Address;
import com.vengine.kk.sap.client.account.Contact;
import com.vengine.kk.sap.client.account.DuplicationResult;
import com.vengine.kk.sap.client.account.request.CreateAccountRequest;
import com.vengine.kk.sap.client.account.request.UpdateAccountRequest;
import com.vengine.kk.sap.common.model.SapQuery;

import java.util.Collections;
import java.util.List;

/**
 * Mock implementation of {@link AccountClientInterface} for use in tests.
 * All methods return empty collections or null by default.
 * TODO: override in tests
 */
public class AccountClientMock implements AccountClientInterface {

    public AccountClientMock() {
    }

    @Override
    public List<Account> fetch(SapQuery query) {
        // TODO: override in tests
        return Collections.emptyList();
    }

    @Override
    public Account fetchByUUID(String uuid) {
        // TODO: override in tests
        return null;
    }

    @Override
    public Account fetchById(String id) {
        // TODO: override in tests
        return null;
    }

    @Override
    public List<Account> fetchByTargetGroup(String targetGroupId) {
        // TODO: override in tests
        return Collections.emptyList();
    }

    @Override
    public DuplicationResult checkDuplication(String name, String street, String city, String country) {
        // TODO: override in tests
        return null;
    }

    @Override
    public List<Account> fetchByIds(List<String> ids) {
        // TODO: override in tests
        return Collections.emptyList();
    }

    @Override
    public Account create(CreateAccountRequest request) {
        // TODO: override in tests
        return null;
    }

    @Override
    public Account update(String uuid, UpdateAccountRequest request) {
        // TODO: override in tests
        return null;
    }

    @Override
    public void deleteAddress(String accountUuid, String addressUuid) {
        // TODO: override in tests
    }

    @Override
    public void deleteContact(String accountUuid, String contactUuid) {
        // TODO: override in tests
    }

    @Override
    public void createAddress(String accountUuid, Address address) {
        // TODO: override in tests
    }

    @Override
    public void createContact(String accountUuid, Contact contact) {
        // TODO: override in tests
    }
}
