package com.jpmc.midascore.service;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LedgerService {

    private final UserRepository userRepository;
    private final TransactionRecordRepository transactionRecordRepository;

    public LedgerService(UserRepository userRepository,
                         TransactionRecordRepository transactionRecordRepository) {
        this.userRepository = userRepository;
        this.transactionRecordRepository = transactionRecordRepository;
    }

    @Transactional
    public boolean process(Transaction tx) {
        if (tx == null || tx.getAmount() <= 0) {
            return false;
        }

        UserRecord sender = userRepository.findById(tx.getSenderId());
        UserRecord recipient = userRepository.findById(tx.getRecipientId());
        if (sender == null || recipient == null) {
            return false; // invalid ids
        }

        if (sender.getBalance() < tx.getAmount()) {
            return false; // insufficient funds
        }

        // apply
        sender.setBalance(sender.getBalance() - tx.getAmount());
        recipient.setBalance(recipient.getBalance() + tx.getAmount());
        userRepository.save(sender);
        userRepository.save(recipient);

        // record transaction
        TransactionRecord record = new TransactionRecord();
        record.setSender(sender);
        record.setRecipient(recipient);
        record.setAmount(tx.getAmount());
        transactionRecordRepository.save(record);
        return true;
    }
}
