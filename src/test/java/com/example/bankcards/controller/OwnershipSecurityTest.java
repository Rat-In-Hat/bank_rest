package com.example.bankcards.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OwnershipSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void userCannotAccessAnotherUsersCard() throws Exception {
        User user1 = userRepository.save(new User("user1", "hash", "ROLE_USER", Instant.now()));
        User user2 = userRepository.save(new User("user2", "hash", "ROLE_USER", Instant.now()));
        cardRepository.save(new Card(
                user1,
                "1111222233334444",
                LocalDate.now().plusDays(30),
                CardStatus.ACTIVE,
                new BigDecimal("100.00"),
                Instant.now()
        ));
        Card чужаяCard = cardRepository.save(new Card(
                user2,
                "5555666677778888",
                LocalDate.now().plusDays(30),
                CardStatus.ACTIVE,
                new BigDecimal("100.00"),
                Instant.now()
        ));

        mockMvc.perform(get("/api/cards/{cardId}", чужаяCard.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void userCannotTransferFromAnotherUsersCard() throws Exception {
        User user1 = userRepository.save(new User("user1", "hash", "ROLE_USER", Instant.now()));
        User user2 = userRepository.save(new User("user2", "hash", "ROLE_USER", Instant.now()));
        Card user1Card = cardRepository.save(new Card(
                user1,
                "9999000011112222",
                LocalDate.now().plusDays(30),
                CardStatus.ACTIVE,
                new BigDecimal("100.00"),
                Instant.now()
        ));
        Card чужаяCard = cardRepository.save(new Card(
                user2,
                "3333444455556666",
                LocalDate.now().plusDays(30),
                CardStatus.ACTIVE,
                new BigDecimal("100.00"),
                Instant.now()
        ));

        String body = objectMapper.writeValueAsString(new TransferRequestPayload(
                чужаяCard.getId(),
                user1Card.getId(),
                new BigDecimal("10.00")
        ));

        mockMvc.perform(post("/api/transfers")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isForbidden());
    }

    private record TransferRequestPayload(Long fromCardId, Long toCardId, BigDecimal amount) {
    }
}
