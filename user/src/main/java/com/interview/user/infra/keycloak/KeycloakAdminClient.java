package com.interview.user.infra.keycloak;

import com.interview.common.exception.ErrorCode;
import com.interview.common.exception.InterviewException;
import com.interview.user.controller.dto.UpdateKeycloakUserRequest;
import com.interview.user.infra.config.KeycloakProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class KeycloakAdminClient {
    private static final Set<String> MANAGED_ROLES = Set.of("ADMIN", "NORMAL_USER");

    private final RestClient restClient;
    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final String registrationId;

    public KeycloakAdminClient(RestClient keycloakAdminRestClient,
                               OAuth2AuthorizedClientManager authorizedClientManager,
                               KeycloakProperties properties) {
        this.restClient = keycloakAdminRestClient;
        this.authorizedClientManager = authorizedClientManager;
        this.registrationId = properties.backend().registrationId();
    }

    public KeycloakUser updateUser(String userId, UpdateKeycloakUserRequest request) {
        try {
            String token = accessToken();
            Map<String, Object> user = getUser(userId, token);
            user.put("username", request.username());
            user.put("email", request.email());
            user.put("firstName", request.firstName());
            user.put("lastName", request.lastName());
            user.put("enabled", request.enabled());
            user.put("attributes", request.attributes() == null ? Map.of() : request.attributes());

            restClient.put()
                    .uri("/users/{id}", userId)
                    .headers(headers -> headers.setBearerAuth(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(user)
                    .retrieve()
                    .toBodilessEntity();

            replaceManagedRealmRoles(userId, request.roles(), token);
            return new KeycloakUser(userId, request.username(), request.email(), request.firstName(),
                    request.lastName(), request.enabled(), Set.copyOf(request.roles()),
                    request.attributes() == null ? Map.of() : Map.copyOf(request.attributes()));
        } catch (RestClientException | IllegalStateException exception) {
            throw new InterviewException(ErrorCode.KEYCLOAK_OPERATION_FAILED, exception,
                    ErrorCode.KEYCLOAK_OPERATION_FAILED.getMessageKey());
        }
    }

    private Map<String, Object> getUser(String userId, String token) {
        Map<String, Object> user = restClient.get()
                .uri("/users/{id}", userId)
                .headers(headers -> headers.setBearerAuth(token))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
        if (user == null) {
            throw new IllegalStateException("Keycloak returned an empty user");
        }
        return new HashMap<>(user);
    }

    private void replaceManagedRealmRoles(String userId, Set<String> desiredRoles, String token) {
        List<RoleRepresentation> desired = desiredRoles.stream()
                .map(role -> getRealmRole(role, token))
                .collect(Collectors.toList());
        List<RoleRepresentation> assignedRoles = restClient.get()
                .uri("/users/{id}/role-mappings/realm", userId)
                .headers(headers -> headers.setBearerAuth(token))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        List<RoleRepresentation> managedAssigned = assignedRoles == null ? List.of() : assignedRoles.stream()
                .filter(role -> MANAGED_ROLES.contains(role.name()))
                .toList();
        if (!managedAssigned.isEmpty()) {
            restClient.method(org.springframework.http.HttpMethod.DELETE)
                    .uri("/users/{id}/role-mappings/realm", userId)
                    .headers(headers -> headers.setBearerAuth(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(managedAssigned)
                    .retrieve()
                    .toBodilessEntity();
        }

        restClient.post()
                .uri("/users/{id}/role-mappings/realm", userId)
                .headers(headers -> headers.setBearerAuth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .body(desired)
                .retrieve()
                .toBodilessEntity();
    }

    private RoleRepresentation getRealmRole(String role, String token) {
        RoleRepresentation representation = restClient.get()
                .uri("/roles/{role}", role)
                .headers(headers -> headers.setBearerAuth(token))
                .retrieve()
                .body(RoleRepresentation.class);
        if (representation == null) {
            throw new IllegalStateException("Keycloak returned an empty role");
        }
        return representation;
    }

    private String accessToken() {
        OAuth2AuthorizedClient client = authorizedClientManager.authorize(
                OAuth2AuthorizeRequest.withClientRegistrationId(registrationId)
                        .principal("keycloak-admin-service")
                        .build());
        if (client == null || client.getAccessToken() == null) {
            throw new IllegalStateException("Unable to obtain Keycloak admin access token");
        }
        return client.getAccessToken().getTokenValue();
    }

    public record KeycloakUser(
            String id,
            String username,
            String email,
            String firstName,
            String lastName,
            boolean enabled,
            Set<String> roles,
            Map<String, List<String>> attributes
    ) {
    }

    private record RoleRepresentation(String id, String name) {
    }
}
