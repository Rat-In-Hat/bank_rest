package com.example.bankcards.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class TransferResponse {
    private final Long id;
    private final Long fromCardId;
    private final Long toCardId;
    private final BigDecimal amount;
    private final Instant createdAt;

    public TransferResponse(Long id, Long fromCardId, Long toCardId, BigDecimal amount, Instant createdAt) {
        this.id = id;
        this.fromCardId = fromCardId;
        this.toCardId = toCardId;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getFromCardId() {
        return fromCardId;
    }

    public Long getToCardId() {
        return toCardId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
