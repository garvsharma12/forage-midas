package com.jpmc.midascore;

import com.jpmc.midascore.dto.TransactionDTO;
import com.jpmc.midascore.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.math.BigDecimal;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "app.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
@EmbeddedKafka(partitions = 1, topics = {"midas-transactions"})
public class TransactionIntegrationTest {

    @Autowired
    private org.springframework.kafka.core.KafkaTemplate<String, TransactionDTO> kafkaTemplate;

    @Autowired
    private TransactionRepository repository;

    @Test
    void whenMessagePublished_thenProcessedAndSaved() {
        var dto = new TransactionDTO("ext-1", new BigDecimal("100.00"), "USD", "test");
        kafkaTemplate.send("midas-transactions", dto.getExternalId(), dto);
        await().until(() -> repository.findByExternalId("ext-1").isPresent());
        assertTrue(repository.findByExternalId("ext-1").isPresent());
    }
}
