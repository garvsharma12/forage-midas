package com.jpmc.midascore.service;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class RawTransactionService {
    private final UserRepository userRepository;
    private final TransactionRecordRepository recordRepository;
    private final IncentiveClient incentiveClient;

    public RawTransactionService(UserRepository userRepository, TransactionRecordRepository recordRepository, IncentiveClient incentiveClient) {
        this.userRepository = userRepository;
        this.recordRepository = recordRepository;
        this.incentiveClient = incentiveClient;
    }

    @Transactional
    public boolean process(Transaction tx) {
        // Validate sender/recipient exist
        UserRecord sender = userRepository.findById(tx.getSenderId());
        UserRecord recipient = userRepository.findById(tx.getRecipientId());
        if (sender == null || recipient == null) {
            return false;
        }

        // Validate sufficient balance
        float amount = tx.getAmount();
        if (amount <= 0f || sender.getBalance() < amount) {
            return false;
        }

        // Fetch incentive from external API
        float incentive = 0f;
        try {
            incentive = incentiveClient.fetchIncentive(new com.jpmc.midascore.foundation.Transaction(
                    tx.getSenderId(), tx.getRecipientId(), tx.getAmount()
            ));
            if (incentive < 0f) incentive = 0f;
        } catch (Exception ex) {
            // If the external service fails, treat incentive as 0 and continue
            incentive = 0f;
        }

        // Apply balance updates (note: sender pays only original amount; recipient gets amount + incentive)
        sender.setBalance(sender.getBalance() - amount);
        recipient.setBalance(recipient.getBalance() + amount + incentive);

        // Persist entities
        userRepository.save(sender);
        userRepository.save(recipient);

        TransactionRecord record = new TransactionRecord();
        record.setSender(sender);
        record.setRecipient(recipient);
    record.setAmount(amount);
    record.setIncentive(incentive);
        recordRepository.save(record);
        return true;
    }
}
