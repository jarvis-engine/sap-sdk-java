package com.vengine.kk.sap.demo.controller;

import com.vengine.kk.sap.client.account.Account;
import com.vengine.kk.sap.client.account.AccountClient;
import com.vengine.kk.sap.client.account.DuplicationResult;
import com.vengine.kk.sap.common.model.SapQuery;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/demo/accounts")
public class AccountController {

    private final AccountClient accountClient;

    public AccountController(AccountClient accountClient) {
        this.accountClient = accountClient;
    }

    @GetMapping
    public List<Account> list(@RequestParam(defaultValue = "10") String limit) {
        SapQuery query = new SapQuery();
        query.setLimit(limit);
        return accountClient.fetch(query);
    }

    @GetMapping("/{uuid}")
    public Account getByUuid(@PathVariable String uuid) {
        return accountClient.fetchByUUID(uuid);
    }

    @GetMapping("/check-duplicate")
    public DuplicationResult checkDuplicate(
            @RequestParam String name,
            @RequestParam String street,
            @RequestParam String city,
            @RequestParam String country) {
        return accountClient.checkDuplication(name, street, city, country);
    }
}
