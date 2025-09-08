package com.jpmc.midascore.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.entity.TransactionEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class IncentiveClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public IncentiveClient(RestTemplate restTemplate, @Value("${incentive.base-url:http://localhost:8080}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IncentiveResponse {
        public float amount;
    }

    public float fetchIncentive(Transaction tx) {
        IncentiveResponse response = restTemplate.postForObject(baseUrl + "/incentive", tx, IncentiveResponse.class);
        return response == null ? 0f : Math.max(0f, response.amount);
    }

    // Overload to satisfy existing TransactionService integration that operates on TransactionEntity
    // For this flow, sender/recipient are unknown; we pass only the amount to compute incentive (if needed).
    public void incentivize(TransactionEntity entity) {
        if (entity == null || entity.getAmount() == null) return;
        try {
            // Best-effort call; ignore result. Convert BigDecimal to float safely.
            float amt = entity.getAmount().floatValue();
            fetchIncentive(new Transaction(0L, 0L, amt));
        } catch (Exception ignored) {
            // Swallow exceptions; caller handles status updates.
        }
    }
}

