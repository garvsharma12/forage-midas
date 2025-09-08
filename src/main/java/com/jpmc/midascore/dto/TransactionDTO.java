package com.jpmc.midascore.dto;

import java.math.BigDecimal;

public class TransactionDTO {
    private String externalId;
    private BigDecimal amount;
    private String currency;
    private String description;

    public TransactionDTO() {

    }
    public TransactionDTO(String externalId, BigDecimal amount, String currency, String description) {
        this.externalId = externalId;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
