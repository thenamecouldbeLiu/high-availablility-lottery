package com.interview.user.controller;

import com.interview.common.response.Response;
import com.interview.user.controller.dto.KeycloakUserResponse;
import com.interview.user.controller.dto.UpdateKeycloakUserRequest;
import com.interview.user.service.KeycloakUserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/keycloak/users")
@SecurityRequirement(name = "clientCredentials")
public class KeycloakUserController {
    private final KeycloakUserService service;

    public KeycloakUserController(KeycloakUserService service) {
        this.service = service;
    }

    @PutMapping("/{keycloakUserId}")
    public Response<KeycloakUserResponse> update(
            @PathVariable String keycloakUserId,
            @Valid @RequestBody UpdateKeycloakUserRequest request) {
        return Response.success(service.update(keycloakUserId, request));
    }
}
