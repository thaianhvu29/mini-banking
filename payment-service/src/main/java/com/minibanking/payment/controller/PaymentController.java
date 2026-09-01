package com.minibanking.payment.controller;

import com.minibanking.payment.client.TransactionClient;
import com.minibanking.payment.entity.Payment;
import com.minibanking.payment.repository.PaymentRepository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final TransactionClient transactionClient;
    private final PaymentRepository paymentRepository;

    public PaymentController(
            TransactionClient transactionClient,
            PaymentRepository paymentRepository
    ) {
        this.transactionClient = transactionClient;
        this.paymentRepository = paymentRepository;
    }

    @PostMapping
    public ResponseEntity<?> createPayment(
            @Valid @RequestBody CreatePaymentRequest request
    ) {

        Map<String, Object> transaction =
                transactionClient.getTransaction(
                        request.transactionId()
                );

        String transactionStatus =
                String.valueOf(transaction.get("status"));

        if (!"CREATED".equals(transactionStatus)) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error",
                            "Transaction is not available for payment"
                    ));
        }

        Payment payment = new Payment(
                request.transactionId(),
                request.amount(),
                "PROCESSED"
        );

        Payment savedPayment =
                paymentRepository.save(payment);

        return ResponseEntity.ok(savedPayment);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPayment(
            @PathVariable Long id
    ) {
        return paymentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public record CreatePaymentRequest(
            @NotNull Long transactionId,
            @NotNull @Positive BigDecimal amount
    ) {
    }
}
