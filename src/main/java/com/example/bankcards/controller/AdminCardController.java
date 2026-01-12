package com.example.bankcards.controller;

import com.example.bankcards.dto.AdminCardCreateRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.CardStatusUpdateRequest;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/cards")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCardController {
    private final CardService cardService;

    public AdminCardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    public ResponseEntity<List<CardResponse>> listCards() {
        return ResponseEntity.ok(cardService.listAllCards());
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<CardResponse> getCard(@PathVariable Long cardId) {
        try {
            return ResponseEntity.ok(cardService.getCard(cardId));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    @PostMapping
    public ResponseEntity<CardResponse> createCard(@Valid @RequestBody AdminCardCreateRequest request) {
        try {
            CardResponse response = cardService.createCard(
                    request.getUserId(),
                    request.getCardNumber(),
                    request.getExpirationDate(),
                    request.getInitialBalance()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            throw toResponseStatusException(ex);
        }
    }

    @PutMapping("/{cardId}/status")
    public ResponseEntity<CardResponse> updateStatus(
            @PathVariable Long cardId,
            @Valid @RequestBody CardStatusUpdateRequest request
    ) {
        try {
            return ResponseEntity.ok(cardService.updateStatus(cardId, request.getStatus()));
        } catch (IllegalArgumentException ex) {
            throw toResponseStatusException(ex);
        }
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long cardId) {
        try {
            cardService.deleteCard(cardId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    private ResponseStatusException toResponseStatusException(IllegalArgumentException ex) {
        String message = ex.getMessage() == null ? "Invalid request" : ex.getMessage();
        if (message.toLowerCase().contains("not found")) {
            return new ResponseStatusException(HttpStatus.NOT_FOUND, message, ex);
        }
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message, ex);
    }
}
