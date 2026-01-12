package com.example.bankcards.service;

import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public CardService(CardRepository cardRepository, UserRepository userRepository) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CardResponse createCard(Long userId, String cardNumber, LocalDate expirationDate, BigDecimal initialBalance) {
        validateCardNumber(cardNumber);
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (cardRepository.findByCardNumber(cardNumber).isPresent()) {
            throw new IllegalArgumentException("Card number already exists");
        }
        Card card = new Card(owner, cardNumber, expirationDate, CardStatus.ACTIVE, normalizeBalance(initialBalance), Instant.now());
        Card saved = cardRepository.save(card);
        return toResponse(saved);
    }

    @Transactional
    public CardResponse updateStatus(Long cardId, CardStatus status) {
        Card card = getCardEntity(cardId);
        card.setStatus(status);
        return toResponse(cardRepository.save(card));
    }

    @Transactional
    public CardResponse updateCard(Long cardId, LocalDate expirationDate) {
        Card card = getCardEntity(cardId);
        validateExpirationDate(expirationDate);
        card.setExpirationDate(expirationDate);
        return toResponse(cardRepository.save(card));
    }

    @Transactional(readOnly = true)
    public CardResponse getCard(Long cardId) {
        return toResponse(getCardEntity(cardId));
    }

    @Transactional(readOnly = true)
    public List<CardResponse> listAllCards() {
        return cardRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CardResponse> listCardsForUser(Long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return cardRepository.findByOwner(owner).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteCard(Long cardId) {
        if (!cardRepository.existsById(cardId)) {
            throw new IllegalArgumentException("Card not found");
        }
        cardRepository.deleteById(cardId);
    }

    public Card getCardEntity(Long cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found"));
    }

    private void validateCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Card number is required");
        }
        if (cardNumber.length() < 12) {
            throw new IllegalArgumentException("Card number must be at least 12 digits");
        }
    }

    private BigDecimal normalizeBalance(BigDecimal balance) {
        if (balance == null) {
            return BigDecimal.ZERO;
        }
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Balance cannot be negative");
        }
        return balance;
    }

    private void validateExpirationDate(LocalDate expirationDate) {
        if (expirationDate == null) {
            throw new IllegalArgumentException("Expiration date is required");
        }
        if (expirationDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Expiration date must be in the future");
        }
    }

    private CardResponse toResponse(Card card) {
        return new CardResponse(
                card.getId(),
                card.getOwner().getId(),
                card.getMaskedNumber(),
                card.getExpirationDate(),
                card.getStatus(),
                card.getBalance()
        );
    }
}
