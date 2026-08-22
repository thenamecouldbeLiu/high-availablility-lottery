package com.interview.user.infra.security;

import java.util.Set;

public record AuthenticatedUser(
        String subject,
        String username,
        String email,
        Set<String> roles
) {
    public boolean isAdmin() {
        return roles.contains("ADMIN");
    }
}
