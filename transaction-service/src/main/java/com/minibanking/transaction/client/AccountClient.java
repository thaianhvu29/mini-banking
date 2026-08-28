package com.minibanking.transaction.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class AccountClient {

    private final RestClient restClient;

    public AccountClient() {
        this.restClient = RestClient.create("http://account-service:8081");
    }

    public Map<String, Object> getAccount(Long accountId) {
        return restClient.get()
                .uri("/api/accounts/{id}", accountId)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }
}
