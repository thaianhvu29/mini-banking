package com.minibanking.account.controller;

import com.minibanking.account.entity.Account;
import com.minibanking.account.repository.AccountRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountRepository accountRepository;

    public AccountController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(
            @RequestBody CreateAccountRequest request) {

        Account account = new Account(
                request.ownerName(),
                request.initialBalance(),
                "ACTIVE"
        );

        return ResponseEntity.ok(accountRepository.save(account));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccount(@PathVariable Long id) {

        return accountRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public record CreateAccountRequest(
            String ownerName,
            BigDecimal initialBalance
    ) {
    }
}
