package com.interview.user.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "app.openapi")
public record OpenApiProperties(
        String title,
        String version,
        OAuth2 oauth2) {

    public record OAuth2(
            String tokenUrl,
            Map<String, String> scopes) {
    }
}
