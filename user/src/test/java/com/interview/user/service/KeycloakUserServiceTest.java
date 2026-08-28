package com.interview.user.service;

import com.interview.user.controller.dto.UpdateKeycloakUserRequest;
import com.interview.user.domain.User;
import com.interview.user.infra.keycloak.KeycloakAdminClient;
import com.interview.user.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KeycloakUserServiceTest {
    private final KeycloakAdminClient keycloakClient = mock(KeycloakAdminClient.class);
    private final UserRepository repository = mock(UserRepository.class);
    private final KeycloakUserService service = new KeycloakUserService(keycloakClient, repository);

    @Test
    void updatesKeycloakAndSynchronizesLocallyStoredFields() {
        User user = new User("keycloak-id", "old-name", "old@example.com", "Old", true);
        var request = new UpdateKeycloakUserRequest(
                "alice", "alice@example.com", "Alice", "Chen", "Alice Chen", false,
                Set.of("ADMIN"), Map.of("locale", List.of("zh-TW")));
        var keycloakUser = new KeycloakAdminClient.KeycloakUser(
                "keycloak-id", "alice", "alice@example.com", "Alice", "Chen", false,
                Set.of("ADMIN"), Map.of("locale", List.of("zh-TW")));
        when(repository.findByKeycloakSubject("keycloak-id")).thenReturn(Optional.of(user));
        when(keycloakClient.updateUser("keycloak-id", request)).thenReturn(keycloakUser);

        var response = service.update("keycloak-id", request);

        verify(keycloakClient).updateUser("keycloak-id", request);
        assertThat(response.roles()).containsExactly("ADMIN");
        assertThat(response.localUser().username()).isEqualTo("alice");
        assertThat(response.localUser().email()).isEqualTo("alice@example.com");
        assertThat(response.localUser().displayName()).isEqualTo("Alice Chen");
        assertThat(response.localUser().enabled()).isFalse();
    }
}
