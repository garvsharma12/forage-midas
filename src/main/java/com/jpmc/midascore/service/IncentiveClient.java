package com.jpmc.midascore.service;

import com.jpmc.midascore.entity.TransactionEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class IncentiveClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public IncentiveClient(RestTemplate restTemplate, @Value("${incentive.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public void incentivize(TransactionEntity tx) {
        var payload = Map.of(
                "transactionId", tx.getExternalId(),
                "amount", tx.getAmount(),
                "currency", tx.getCurrency()
        );
        // example call: POST {baseUrl}/incentivize
        restTemplate.postForEntity(baseUrl + "/incentivize", payload, Void.class);
        // If the remote service returns 4xx/5xx, an exception will be thrown and handled by caller.
    }
}

