package com.jpmc.midascore.repository;

import com.jpmc.midascore.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID> {
    Optional<TransactionEntity> findByExternalId(String externalId);
}
