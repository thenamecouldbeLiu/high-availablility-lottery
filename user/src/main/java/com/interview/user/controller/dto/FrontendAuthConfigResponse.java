package com.interview.user.controller.dto;

import com.interview.user.infra.config.KeycloakProperties;

public record FrontendAuthConfigResponse(
        String clientId,
        String authorizationUri,
        String tokenUri,
        String redirectUri,
        String scopes,
        String pkceMethod
) {
    public static FrontendAuthConfigResponse from(KeycloakProperties.Frontend frontend) {
        return new FrontendAuthConfigResponse(frontend.clientId(), frontend.authorizationUri(),
                frontend.tokenUri(), frontend.redirectUri(), frontend.scopes(), frontend.pkceMethod());
    }
}
