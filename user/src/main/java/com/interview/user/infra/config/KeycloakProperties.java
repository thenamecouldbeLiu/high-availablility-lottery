package com.interview.user.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.keycloak")
public record KeycloakProperties(Backend backend, Frontend frontend) {
    public record Backend(String registrationId, String clientId, String adminBaseUri) {
    }

    public record Frontend(
            String clientId,
            String authorizationUri,
            String tokenUri,
            String redirectUri,
            String scopes,
            String pkceMethod
    ) {
    }
}
