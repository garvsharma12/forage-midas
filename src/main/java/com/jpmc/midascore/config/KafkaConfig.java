package com.jpmc.midascore.config;

import com.jpmc.midascore.dto.TransactionDTO;
import com.jpmc.midascore.foundation.Transaction;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:${app.kafka.bootstrap-servers:${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}}}")
    private String bootstrapServers;
    @Value("${spring.kafka.consumer.group-id:}")
    private String consumerGroupId;
    @Value("${app.kafka.listeners.auto-startup:true}")
    private boolean listenersAutoStartup;
    @Value("${general.kafka-topic:midas-transactions}")
    private String generalTopic;
    @Value("${app.kafka.topic.transactions:midas-transactions}")
    private String transactionsTopic;

    // Consumer
    @Bean
    public ConsumerFactory<String, TransactionDTO> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        if (consumerGroupId != null && !consumerGroupId.isBlank()) {
            props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        }
        JsonDeserializer<TransactionDTO> valueDeserializer = new JsonDeserializer<>(TransactionDTO.class);
        valueDeserializer.addTrustedPackages("com.jpmc.midascore");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), valueDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TransactionDTO> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, TransactionDTO> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(org.springframework.kafka.listener.ContainerProperties.AckMode.MANUAL);
        factory.setConcurrency(3);
        factory.setAutoStartup(listenersAutoStartup);
        return factory;
    }

    // Producer
    @Bean
    public ProducerFactory<String, TransactionDTO> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, TransactionDTO> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // Additional producer for test helper that uses foundation.Transaction
    @Bean
    public ProducerFactory<String, Transaction> transactionProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, Transaction> transactionKafkaTemplate() {
        return new KafkaTemplate<>(transactionProducerFactory());
    }

    // Admin and Topics (ensure topics exist for tests and local dev)
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(org.apache.kafka.clients.admin.AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configs.put("request.timeout.ms", 5000);
        configs.put("default.api.timeout.ms", 5000);
        KafkaAdmin admin = new KafkaAdmin(configs);
        admin.setFatalIfBrokerNotAvailable(false);
        return admin;
    }

    @Bean
    public org.apache.kafka.clients.admin.NewTopic topicGeneral() {
        return TopicBuilder.name(generalTopic).partitions(1).replicas(1).build();
    }

    @Bean
    public org.apache.kafka.clients.admin.NewTopic topicTransactions() {
        return TopicBuilder.name(transactionsTopic).partitions(1).replicas(1).build();
    }
}

