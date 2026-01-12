package com.example.bankcards.dto;

import java.math.BigDecimal;

public class CardBalanceResponse {
    private final Long cardId;
    private final BigDecimal balance;

    public CardBalanceResponse(Long cardId, BigDecimal balance) {
        this.cardId = cardId;
        this.balance = balance;
    }

    public Long getCardId() {
        return cardId;
    }

    public BigDecimal getBalance() {
        return balance;
    }
}
