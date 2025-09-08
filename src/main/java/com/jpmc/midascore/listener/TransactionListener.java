package com.jpmc.midascore.listener;

import com.jpmc.midascore.dto.TransactionDTO;
import com.jpmc.midascore.service.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class TransactionListener {
    private static final Log log = LogFactory.getLog(TransactionListener.class);
    private final TransactionService transactionService;

    public TransactionListener(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @KafkaListener(topics = "${app.kafka.topic.transactions}", containerFactory = "kafkaListenerContainerFactory")
    public void onMessage(TransactionDTO dto, Acknowledgment ack) {
        try {
            transactionService.processIncomingTransaction(dto);
            // only acknowledge after success
            ack.acknowledge();
        } catch (Exception e) {
            // log and do NOT acknowledge so the record can be retried (depending on broker and container config)
            log.error("Failed to process transaction: " + dto.getExternalId(), e);
            // Optionally: push DTO to a dead-letter topic or move to a DLQ after repeated failures.
        }
    }
}

