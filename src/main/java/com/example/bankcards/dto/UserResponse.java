package com.example.bankcards.dto;

import java.time.Instant;

public class UserResponse {
    private final Long id;
    private final String username;
    private final String role;
    private final Instant createdAt;

    public UserResponse(Long id, String username, String role, Instant createdAt) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
