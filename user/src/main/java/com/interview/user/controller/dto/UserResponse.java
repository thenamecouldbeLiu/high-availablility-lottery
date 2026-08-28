package com.interview.user.controller.dto;

import com.interview.user.domain.User;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String keycloakSubject,
        String username,
        String email,
        String displayName,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getKeycloakSubject(), user.getUsername(),
                user.getEmail(), user.getDisplayName(), user.isEnabled(),
                user.getCreatedAt(), user.getUpdatedAt());
    }
}
