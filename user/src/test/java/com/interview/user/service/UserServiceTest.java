package com.interview.user.service;

import com.interview.common.exception.InterviewException;
import com.interview.user.domain.User;
import com.interview.user.domain.UserRole;
import com.interview.user.infra.security.AuthenticatedUser;
import com.interview.user.infra.security.UserRequestContext;
import com.interview.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {
    private final UserRepository repository = mock(UserRepository.class);
    private final UserService service = new UserService(repository);

    @AfterEach
    void clearContext() {
        UserRequestContext.clear();
    }

    @Test
    void normalUserCanReadOwnUser() {
        UUID id = UUID.randomUUID();
        User user = user("own-sub");
        when(repository.findById(id)).thenReturn(Optional.of(user));
        UserRequestContext.set(new AuthenticatedUser("own-sub", "alice", "alice@example.com",
                Set.of("NORMAL_USER")));

        assertThat(service.get(id).keycloakSubject()).isEqualTo("own-sub");
    }

    @Test
    void normalUserCannotReadAnotherUser() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(user("another-sub")));
        UserRequestContext.set(new AuthenticatedUser("own-sub", "alice", "alice@example.com",
                Set.of("NORMAL_USER")));

        assertThatThrownBy(() -> service.get(id))
                .isInstanceOf(InterviewException.class)
                .extracting(error -> ((InterviewException) error).getErrorCode().getCode())
                .isEqualTo("ACCESS_DENIED");
    }

    @Test
    void getCurrentReturnsExistingUser() {
        User user = user("own-sub");
        when(repository.findByKeycloakSubject("own-sub")).thenReturn(Optional.of(user));
        UserRequestContext.set(new AuthenticatedUser("own-sub", "alice", "alice@example.com",
                Set.of("NORMAL_USER")));

        assertThat(service.getOrCreateCurrentUser().keycloakSubject()).isEqualTo("own-sub");
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any(User.class));
    }

    @Test
    void getCurrentCreatesUserFromTokenWhenMissing() {
        when(repository.findByKeycloakSubject("new-sub")).thenReturn(Optional.empty());
        when(repository.save(org.mockito.ArgumentMatchers.any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        UserRequestContext.set(new AuthenticatedUser("new-sub", "alice", "alice@example.com",
                Set.of("ADMIN")));

        var response = service.getOrCreateCurrentUser();

        assertThat(response.keycloakSubject()).isEqualTo("new-sub");
        assertThat(response.username()).isEqualTo("alice");
        assertThat(response.email()).isEqualTo("alice@example.com");
        assertThat(response.displayName()).isEqualTo("alice");
        assertThat(response.role()).isEqualTo(UserRole.ADMIN);
        assertThat(response.enabled()).isTrue();
        verify(repository).save(org.mockito.ArgumentMatchers.any(User.class));
    }

    private User user(String subject) {
        return new User(subject, subject, subject + "@example.com", subject,
                UserRole.NORMAL_USER, true);
    }
}
