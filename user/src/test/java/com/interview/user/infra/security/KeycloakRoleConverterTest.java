package com.interview.user.infra.security;

import com.interview.user.infra.config.KeycloakProperties;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class KeycloakRoleConverterTest {
    private final KeycloakRoleConverter converter = new KeycloakRoleConverter(
            new KeycloakProperties(
                    new KeycloakProperties.Backend("keycloak-backend", "user-service"),
                    new KeycloakProperties.Frontend(null, null, null, null, null, null)));

    @Test
    void mapsRealmAndClientRoles() {
        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(60),
                Map.of("alg", "none"),
                Map.of(
                        "sub", "subject",
                        "realm_access", Map.of("roles", List.of("Admin")),
                        "resource_access", Map.of("user-service",
                                Map.of("roles", List.of("NormalUser")))));

        assertThat(converter.convert(jwt))
                .extracting(Object::toString)
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_NORMAL_USER");
    }
}
