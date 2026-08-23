package com.interview.lottory.infra.security;

public final class CurrentUserUtil {
    private CurrentUserUtil() {
    }

    public static CurrentUser requireCurrentUser() {
        return CurrentUserContext.require();
    }

    public static String currentSubject() {
        return requireCurrentUser().keycloakSubject();
    }
}
