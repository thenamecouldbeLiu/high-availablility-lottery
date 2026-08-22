package com.interview.user.infra.security;

import java.util.Optional;

public final class UserRequestContext {
    private static final ThreadLocal<AuthenticatedUser> CURRENT = new ThreadLocal<>();

    private UserRequestContext() {
    }

    public static void set(AuthenticatedUser user) {
        CURRENT.set(user);
    }

    public static Optional<AuthenticatedUser> current() {
        return Optional.ofNullable(CURRENT.get());
    }

    public static AuthenticatedUser requireCurrent() {
        return current().orElseThrow(() -> new IllegalStateException("Authenticated user context is missing"));
    }

    public static void clear() {
        CURRENT.remove();
    }
}
