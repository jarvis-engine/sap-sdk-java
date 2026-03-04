package com.vengine.kk.sap.client.account.request;

import com.vengine.kk.sap.client.account.Account;
import com.vengine.kk.sap.client.account.AddressBook;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {

    private Account account;
    private AddressBook addressBook;
}
