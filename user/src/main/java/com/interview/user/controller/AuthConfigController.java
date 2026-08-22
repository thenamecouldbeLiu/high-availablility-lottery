package com.interview.user.controller;

import com.interview.common.response.Response;
import com.interview.user.controller.dto.FrontendAuthConfigResponse;
import com.interview.user.infra.config.KeycloakProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthConfigController {
    private final KeycloakProperties properties;

    public AuthConfigController(KeycloakProperties properties) {
        this.properties = properties;
    }

    @GetMapping("/config")
    public Response<FrontendAuthConfigResponse> config() {
        return Response.success(FrontendAuthConfigResponse.from(properties.frontend()));
    }
}
