package com.interview.user.utils;

import com.interview.user.infra.security.AuthenticatedUser;
import com.interview.user.infra.security.UserRequestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserUtilTest {
    @AfterEach
    void clearContext() {
        UserRequestContext.clear();
    }

    @Test
    void exposesCurrentAuthenticatedUserInformation() {
        UserRequestContext.set(new AuthenticatedUser(
                "subject-1", "alice", "alice@example.com", Set.of("ADMIN")));

        assertThat(UserUtil.currentSubject()).isEqualTo("subject-1");
        assertThat(UserUtil.currentUsername()).isEqualTo("alice");
        assertThat(UserUtil.currentEmail()).isEqualTo("alice@example.com");
        assertThat(UserUtil.isAdmin()).isTrue();
        assertThat(UserUtil.hasRole("ADMIN")).isTrue();
    }
}
