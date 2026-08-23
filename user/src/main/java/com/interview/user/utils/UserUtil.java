package com.interview.user.utils;

import com.interview.user.infra.security.AuthenticatedUser;
import com.interview.user.infra.security.UserRequestContext;

import java.util.Optional;

public final class UserUtil {
    private UserUtil() {
    }

    public static Optional<AuthenticatedUser> currentUser() {
        return UserRequestContext.current();
    }

    public static AuthenticatedUser requireCurrentUser() {
        return UserRequestContext.requireCurrent();
    }

    public static String currentSubject() {
        return requireCurrentUser().subject();
    }

    public static String currentUsername() {
        return requireCurrentUser().username();
    }

    public static String currentEmail() {
        return requireCurrentUser().email();
    }

    public static boolean isAdmin() {
        return requireCurrentUser().isAdmin();
    }

    public static boolean hasRole(String role) {
        return requireCurrentUser().roles().contains(role);
    }
}
