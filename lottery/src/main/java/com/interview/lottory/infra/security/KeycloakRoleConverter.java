package com.interview.lottory.infra.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Component
public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    private final String clientId;

    public KeycloakRoleConverter(@Value("${app.keycloak.client-id}") String clientId) {
        this.clientId = clientId;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Set<String> roles = new HashSet<>();
        addRoles(roles, jwt.getClaimAsMap("realm_access"));
        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
        Object clientAccess = resourceAccess == null ? null : resourceAccess.get(clientId);
        if (clientAccess instanceof Map<?, ?> map) addRoles(roles, map);

        return roles.stream()
                .map(this::toApplicationRole)
                .flatMap(Optional::stream)
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
    }

    private void addRoles(Set<String> target, Map<?, ?> access) {
        if (access == null) return;
        Object value = access.get("roles");
        if (value instanceof Collection<?> roles) {
            roles.stream().filter(Objects::nonNull).map(Object::toString).forEach(target::add);
        }
    }

    private Optional<String> toApplicationRole(String role) {
        String normalized = role.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "ADMIN" -> Optional.of("ADMIN");
            case "NORMALUSER" -> Optional.of("NORMAL_USER");
            default -> Optional.empty();
        };
    }
}
