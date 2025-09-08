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

    public RawTransactionService(UserRepository userRepository, TransactionRecordRepository recordRepository) {
        this.userRepository = userRepository;
        this.recordRepository = recordRepository;
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

        // Apply balance updates
        sender.setBalance(sender.getBalance() - amount);
        recipient.setBalance(recipient.getBalance() + amount);

        // Persist entities
        userRepository.save(sender);
        userRepository.save(recipient);

        TransactionRecord record = new TransactionRecord();
        record.setSender(sender);
        record.setRecipient(recipient);
        record.setAmount(amount);
        recordRepository.save(record);
        return true;
    }
}
