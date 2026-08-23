package com.interview.user.infra.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OpenApiProperties.class)
public class OpenApiConfig {
    public static final String CLIENT_CREDENTIALS_SCHEME = "clientCredentials";

    @Bean
    OpenAPI userOpenApi(OpenApiProperties properties) {
        Scopes scopes = new Scopes();
        if (properties.oauth2().scopes() != null) {
            properties.oauth2().scopes().forEach(scopes::addString);
        }

        SecurityScheme clientCredentials = new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .flows(new OAuthFlows().clientCredentials(new OAuthFlow()
                        .tokenUrl(properties.oauth2().tokenUrl())
                        .scopes(scopes)));

        return new OpenAPI()
                .info(new Info()
                        .title(properties.title())
                        .version(properties.version()))
                .components(new Components()
                        .addSecuritySchemes(CLIENT_CREDENTIALS_SCHEME, clientCredentials));
    }

    @Bean
    GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("user-api")
                .pathsToMatch("/api/users/**", "/api/auth/**")
                .build();
    }
}
