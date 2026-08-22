package com.interview.user.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {
    @Test
    void generatesUuidVersion7BeforePersist() {
        User user = new User("keycloak-sub", "alice", "alice@example.com",
                "Alice", UserRole.NORMAL_USER, true);

        user.create();

        assertThat(user.getId()).isNotNull();
        assertThat(user.getId().version()).isEqualTo(7);
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isEqualTo(user.getCreatedAt());
    }
}
