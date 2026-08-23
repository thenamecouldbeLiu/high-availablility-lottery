package com.interview.lottory.infra.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    private static final String BACKEND_CLIENT = "keycloak-backend";
    private static final String USER_PKCE = "keycloak-user-pkce";

    @Bean
    OpenAPI lotteryOpenApi(
            @Value("${app.swagger.oauth2.authorization-url}") String authorizationUrl,
            @Value("${app.swagger.oauth2.token-url}") String tokenUrl) {
        SecurityScheme backendClientScheme = new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .description("Keycloak backend client credentials")
                .flows(new OAuthFlows().clientCredentials(
                        new OAuthFlow()
                                .tokenUrl(tokenUrl)
                                .scopes(new Scopes().addString("openid", "OpenID Connect"))));

        Scopes userScopes = new Scopes()
                .addString("openid", "OpenID Connect")
                .addString("profile", "User profile")
                .addString("email", "User email");
        SecurityScheme userPkceScheme = new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .description("Keycloak user login with Authorization Code and PKCE S256")
                .flows(new OAuthFlows().authorizationCode(
                        new OAuthFlow()
                                .authorizationUrl(authorizationUrl)
                                .tokenUrl(tokenUrl)
                                .scopes(userScopes)));

        return new OpenAPI()
                .info(new Info()
                        .title("Lottery API")
                        .description("Lottery service API")
                        .version("v1"))
                .components(new Components()
                        .addSecuritySchemes(BACKEND_CLIENT, backendClientScheme)
                        .addSecuritySchemes(USER_PKCE, userPkceScheme))
                // Separate requirements mean either backend credentials OR an authenticated user token.
                .addSecurityItem(new SecurityRequirement().addList(BACKEND_CLIENT))
                .addSecurityItem(new SecurityRequirement().addList(USER_PKCE));
    }
}
