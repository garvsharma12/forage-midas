package com.jpmc.midascore.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions", uniqueConstraints = @UniqueConstraint(columnNames = "external_id"))
public class TransactionEntity {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "external_id", nullable = false, unique = true)
    private String externalId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(length = 3,nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    private Status status;

    private Instant createdAt;
    private Instant updatedAt;

    public enum Status {
        RECEIVED, REJECTED, RECORDED, INCENTIVIZED, INCENTIVIZATION_FAILED
    }

    @PrePersist
    void onCreate() { createdAt = Instant.now(); }

    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }

}

