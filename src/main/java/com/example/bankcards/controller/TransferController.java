package com.example.bankcards.controller;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/transfers")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class TransferController {
    private final TransferService transferService;
    private final CardService cardService;
    private final UserRepository userRepository;

    public TransferController(TransferService transferService, CardService cardService, UserRepository userRepository) {
        this.transferService = transferService;
        this.cardService = cardService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<TransferResponse> createTransfer(@Valid @RequestBody TransferRequest request) {
        User currentUser = loadCurrentUser();
        Card fromCard = loadCard(request.getFromCardId());
        Card toCard = loadCard(request.getToCardId());
        assertOwner(fromCard, currentUser);
        assertOwner(toCard, currentUser);

        try {
            Transfer transfer = transferService.transfer(
                    request.getFromCardId(),
                    request.getToCardId(),
                    request.getAmount()
            );
            TransferResponse response = new TransferResponse(
                    transfer.getId(),
                    transfer.getFromCard().getId(),
                    transfer.getToCard().getId(),
                    transfer.getAmount(),
                    transfer.getCreatedAt()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
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
}
