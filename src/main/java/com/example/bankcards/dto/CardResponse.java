package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CardResponse {
    private final Long id;
    private final Long ownerId;
    private final String maskedNumber;
    private final LocalDate expirationDate;
    private final CardStatus status;
    private final BigDecimal balance;

    public CardResponse(Long id, Long ownerId, String maskedNumber, LocalDate expirationDate, CardStatus status, BigDecimal balance) {
        this.id = id;
        this.ownerId = ownerId;
        this.maskedNumber = maskedNumber;
        this.expirationDate = expirationDate;
        this.status = status;
        this.balance = balance;
    }

    public Long getId() {
        return id;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public String getMaskedNumber() {
        return maskedNumber;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public CardStatus getStatus() {
        return status;
    }

    public BigDecimal getBalance() {
        return balance;
    }
}
