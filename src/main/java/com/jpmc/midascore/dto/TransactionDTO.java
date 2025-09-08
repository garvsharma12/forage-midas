package com.jpmc.midascore.dto;

import java.math.BigDecimal;

public class TransactionDTO {
    private String externalId;
    private BigDecimal amount;
    private String currency;
    private String description;

    public TransactionDTO() {

    }
    public String getExternalId() {
        return externalId;
    }
    public TransactionDTO(String externalId, BigDecimal amount, String currency, String description) {
        this.externalId = externalId;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
    }
}
