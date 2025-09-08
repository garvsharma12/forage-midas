package com.jpmc.midascore.Controller;

import com.jpmc.midascore.dto.TransactionDTO;
import com.jpmc.midascore.entity.TransactionEntity;
import com.jpmc.midascore.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionRepository repository;
    private final KafkaTemplate<String, TransactionDTO> kafkaTemplate;

    @Value("${app.kafka.topic.transactions}")
    private String topic;

    public TransactionController(TransactionRepository repository, KafkaTemplate<String, TransactionDTO> kafkaTemplate) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @GetMapping("/{externalId}")
    public ResponseEntity<TransactionEntity> getByExternalId(@PathVariable String externalId) {
        return repository.findByExternalId(externalId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public Page<TransactionEntity> list(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        return repository.findAll(PageRequest.of(page, size));
    }

    // helper endpoint to publish a sample message to Kafka (useful for local testing)
    @PostMapping("/publish")
    public ResponseEntity<String> publish(@RequestBody TransactionDTO dto) {
        kafkaTemplate.send(topic, dto.getExternalId(), dto);
        return ResponseEntity.accepted().body("published");
    }
}

