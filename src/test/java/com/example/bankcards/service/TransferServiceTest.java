package com.example.bankcards.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.repository.UserRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TransferServiceTest {

    @Autowired
    private TransferService transferService;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void transferMovesFundsAndPersistsTransfer() {
        User user = userRepository.save(new User("alice", "hash", "ROLE_USER", Instant.now()));
        Card fromCard = cardRepository.save(new Card(
                user,
                "1111222233334444",
                LocalDate.now().plusDays(30),
                CardStatus.ACTIVE,
                new BigDecimal("200.00"),
                Instant.now()
        ));
        Card toCard = cardRepository.save(new Card(
                user,
                "5555666677778888",
                LocalDate.now().plusDays(30),
                CardStatus.ACTIVE,
                new BigDecimal("50.00"),
                Instant.now()
        ));

        Transfer transfer = transferService.transfer(fromCard.getId(), toCard.getId(), new BigDecimal("25.00"));

        assertThat(transfer.getId()).isNotNull();
        assertThat(transferRepository.findById(transfer.getId())).isPresent();
        Card updatedFrom = cardRepository.findById(fromCard.getId()).orElseThrow();
        Card updatedTo = cardRepository.findById(toCard.getId()).orElseThrow();
        assertThat(updatedFrom.getBalance()).isEqualByComparingTo("175.00");
        assertThat(updatedTo.getBalance()).isEqualByComparingTo("75.00");
    }

    @Test
    void transferFailsWhenInsufficientFunds() {
        User user = userRepository.save(new User("bob", "hash", "ROLE_USER", Instant.now()));
        Card fromCard = cardRepository.save(new Card(
                user,
                "9999000011112222",
                LocalDate.now().plusDays(30),
                CardStatus.ACTIVE,
                new BigDecimal("10.00"),
                Instant.now()
        ));
        Card toCard = cardRepository.save(new Card(
                user,
                "3333444455556666",
                LocalDate.now().plusDays(30),
                CardStatus.ACTIVE,
                new BigDecimal("10.00"),
                Instant.now()
        ));

        assertThatThrownBy(() -> transferService.transfer(fromCard.getId(), toCard.getId(), new BigDecimal("25.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient funds");
    }
}
