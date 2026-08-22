package com.interview.user.controller.dto;

import com.interview.user.domain.User;
import com.interview.user.domain.UserRole;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String keycloakSubject,
        String username,
        String email,
        String displayName,
        UserRole role,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getKeycloakSubject(), user.getUsername(),
                user.getEmail(), user.getDisplayName(), user.getRole(), user.isEnabled(),
                user.getCreatedAt(), user.getUpdatedAt());
    }
}
