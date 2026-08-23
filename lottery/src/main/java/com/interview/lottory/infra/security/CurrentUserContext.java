package com.interview.lottory.infra.security;

final class CurrentUserContext {
    private static final ThreadLocal<CurrentUser> CURRENT = new ThreadLocal<>();

    private CurrentUserContext() {
    }

    static void set(CurrentUser user) {
        CURRENT.set(user);
    }

    static CurrentUser require() {
        CurrentUser user = CURRENT.get();
        if (user == null) throw new IllegalStateException("Current user has not been initialized");
        return user;
    }

    static void clear() {
        CURRENT.remove();
    }
}
