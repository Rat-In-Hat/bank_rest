package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferService {
    private final CardRepository cardRepository;
    private final TransferRepository transferRepository;

    public TransferService(CardRepository cardRepository, TransferRepository transferRepository) {
        this.cardRepository = cardRepository;
        this.transferRepository = transferRepository;
    }

    @Transactional
    public Transfer transfer(Long fromCardId, Long toCardId, BigDecimal amount) {
        validateAmount(amount);
        if (Objects.equals(fromCardId, toCardId)) {
            throw new IllegalArgumentException("Source and destination cards must differ");
        }

        Card fromCard;
        Card toCard;
        if (fromCardId < toCardId) {
            fromCard = loadCardForUpdate(fromCardId);
            toCard = loadCardForUpdate(toCardId);
        } else {
            toCard = loadCardForUpdate(toCardId);
            fromCard = loadCardForUpdate(fromCardId);
        }

        validateCardForTransfer(fromCard);
        validateCardForTransfer(toCard);
        if (!fromCard.getOwner().getId().equals(toCard.getOwner().getId())) {
            throw new IllegalArgumentException("Transfers allowed only between cards of the same user");
        }
        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        Transfer transfer = new Transfer(fromCard, toCard, amount, Instant.now());
        return transferRepository.save(transfer);
    }

    private Card loadCardForUpdate(Long cardId) {
        return cardRepository.findByIdForUpdate(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found"));
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }

    private void validateCardForTransfer(Card card) {
        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalArgumentException("Card is not active");
        }
        LocalDate today = LocalDate.now();
        if (card.getExpirationDate().isBefore(today)) {
            throw new IllegalArgumentException("Card is expired");
        }
    }
}
