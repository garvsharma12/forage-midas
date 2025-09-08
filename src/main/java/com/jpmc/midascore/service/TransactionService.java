package com.jpmc.midascore.service;

import com.jpmc.midascore.dto.TransactionDTO;
import com.jpmc.midascore.entity.TransactionEntity;
import com.jpmc.midascore.entity.TransactionEntity.Status;
import com.jpmc.midascore.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

@Service
public class TransactionService {

    private final TransactionRepository repository;
    private final IncentiveClient incentiveClient;

    private final Set<String> allowedCurrencies = Set.of("USD", "EUR", "INR");

    public TransactionService(TransactionRepository repository, IncentiveClient incentiveClient) {
        this.repository = repository;
        this.incentiveClient = incentiveClient;
    }

    @Transactional
    public TransactionEntity processIncomingTransaction(TransactionDTO dto) {
        // idempotency check
        var existing = repository.findByExternalId(dto.getExternalId());
        if (existing.isPresent()) {
            return existing.get(); // already processed
        }

        // persist initial received record
        var entity = new TransactionEntity();
        entity.setExternalId(dto.getExternalId());
        entity.setAmount(dto.getAmount());
        entity.setCurrency(dto.getCurrency());
        entity.setStatus(Status.RECEIVED);
        entity = repository.save(entity);

        // simple validation
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            entity.setStatus(Status.REJECTED);
            return repository.save(entity);
        }
        if (!allowedCurrencies.contains(dto.getCurrency())) {
            entity.setStatus(Status.REJECTED);
            return repository.save(entity);
        }

        // mark as recorded
        entity.setStatus(Status.RECORDED);
        entity = repository.save(entity);

        // call incentive REST API (best-effort; failure tracked)
        try {
            incentiveClient.incentivize(entity);
            entity.setStatus(Status.INCENTIVIZED);
        } catch (Exception ex) {
            // log and mark failed; can schedule retry later
            entity.setStatus(Status.INCENTIVIZATION_FAILED);
        }
        return repository.save(entity);
    }
}

