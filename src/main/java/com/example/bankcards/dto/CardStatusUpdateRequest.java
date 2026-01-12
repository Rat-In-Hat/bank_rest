package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import jakarta.validation.constraints.NotNull;

public class CardStatusUpdateRequest {
    @NotNull
    private CardStatus status;

    public CardStatusUpdateRequest() {
    }

    public CardStatus getStatus() {
        return status;
    }

    public void setStatus(CardStatus status) {
        this.status = status;
    }
}
