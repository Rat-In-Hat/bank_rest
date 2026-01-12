package com.example.bankcards.controller;

import com.example.bankcards.dto.CardBalanceResponse;
import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.CardUpdateRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/cards")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class CardController {
    private final CardService cardService;
    private final UserRepository userRepository;

    public CardController(CardService cardService, UserRepository userRepository) {
        this.cardService = cardService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<CardResponse>> listCards() {
        User currentUser = loadCurrentUser();
        return ResponseEntity.ok(cardService.listCardsForUser(currentUser.getId()));
    }

    @PostMapping
    public ResponseEntity<CardResponse> createCard(@Valid @RequestBody CardCreateRequest request) {
        User currentUser = loadCurrentUser();
        try {
            CardResponse response = cardService.createCard(
                    currentUser.getId(),
                    request.getCardNumber(),
                    request.getExpirationDate(),
                    request.getInitialBalance()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            throw toBadRequest(ex);
        }
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<CardResponse> getCard(@PathVariable Long cardId) {
        User currentUser = loadCurrentUser();
        Card card = loadCard(cardId);
        assertOwner(card, currentUser);
        return ResponseEntity.ok(cardService.getCard(cardId));
    }

    @PutMapping("/{cardId}")
    public ResponseEntity<CardResponse> updateCard(
            @PathVariable Long cardId,
            @Valid @RequestBody CardUpdateRequest request
    ) {
        User currentUser = loadCurrentUser();
        Card card = loadCard(cardId);
        assertOwner(card, currentUser);
        try {
            return ResponseEntity.ok(cardService.updateCard(cardId, request.getExpirationDate()));
        } catch (IllegalArgumentException ex) {
            throw toBadRequest(ex);
        }
    }

    @PostMapping("/{cardId}/block")
    public ResponseEntity<CardResponse> requestBlock(@PathVariable Long cardId) {
        User currentUser = loadCurrentUser();
        Card card = loadCard(cardId);
        assertOwner(card, currentUser);
        try {
            return ResponseEntity.ok(cardService.updateStatus(cardId, CardStatus.BLOCKED));
        } catch (IllegalArgumentException ex) {
            throw toBadRequest(ex);
        }
    }

    @GetMapping("/{cardId}/balance")
    public ResponseEntity<CardBalanceResponse> getBalance(@PathVariable Long cardId) {
        User currentUser = loadCurrentUser();
        Card card = loadCard(cardId);
        assertOwner(card, currentUser);
        return ResponseEntity.ok(new CardBalanceResponse(card.getId(), card.getBalance()));
    }

    private User loadCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private Card loadCard(Long cardId) {
        try {
            return cardService.getCardEntity(cardId);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    private void assertOwner(Card card, User user) {
        if (!card.getOwner().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Card does not belong to user");
        }
    }

    private ResponseStatusException toBadRequest(IllegalArgumentException ex) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
    }
}
