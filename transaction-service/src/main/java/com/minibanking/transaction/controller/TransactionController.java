package com.minibanking.transaction.controller;

import com.minibanking.transaction.client.AccountClient;
import com.minibanking.transaction.entity.Transaction;
import com.minibanking.transaction.repository.TransactionRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final AccountClient accountClient;
    private final TransactionRepository transactionRepository;

    public TransactionController(
            AccountClient accountClient,
            TransactionRepository transactionRepository) {

        this.accountClient = accountClient;
        this.transactionRepository = transactionRepository;
    }

    @PostMapping
    public ResponseEntity<?> createTransaction(
            @RequestBody CreateTransactionRequest request) {

        Map<String, Object> account =
                accountClient.getAccount(request.accountId());

        String accountStatus =
                String.valueOf(account.get("status"));

        if (!"ACTIVE".equals(accountStatus)) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error",
                            "Account is not active"
                    ));
        }

        Transaction transaction = new Transaction(
                request.accountId(),
                request.amount(),
                "CREATED"
        );

        Transaction saved =
                transactionRepository.save(transaction);

        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransaction(
            @PathVariable Long id) {

        return transactionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public record CreateTransactionRequest(
            Long accountId,
            BigDecimal amount
    ) {
    }
}
