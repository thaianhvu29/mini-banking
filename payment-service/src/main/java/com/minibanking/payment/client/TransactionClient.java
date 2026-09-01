package com.minibanking.payment.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class TransactionClient {

    private final RestClient restClient;

    public TransactionClient() {
        this.restClient =
                RestClient.create("http://transaction-service:8082");
    }

    public Map<String, Object> getTransaction(Long transactionId) {

        return restClient.get()
                .uri("/api/transactions/{id}", transactionId)
                .retrieve()
                .body(Map.class);
    }
}

