package com.interview.lottory.infra.security;

import java.util.UUID;

public record CurrentUser(
        UUID id,
        String keycloakSubject,
        String username,
        String email,
        String displayName,
        String role,
        boolean enabled
) {
}
