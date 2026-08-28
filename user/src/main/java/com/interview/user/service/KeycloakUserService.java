package com.interview.user.service;

import com.interview.common.exception.ErrorCode;
import com.interview.common.exception.InterviewException;
import com.interview.user.controller.dto.KeycloakUserResponse;
import com.interview.user.controller.dto.UpdateKeycloakUserRequest;
import com.interview.user.controller.dto.UserResponse;
import com.interview.user.domain.User;
import com.interview.user.infra.keycloak.KeycloakAdminClient;
import com.interview.user.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KeycloakUserService {
    private final KeycloakAdminClient keycloakClient;
    private final UserRepository userRepository;

    public KeycloakUserService(KeycloakAdminClient keycloakClient, UserRepository userRepository) {
        this.keycloakClient = keycloakClient;
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public KeycloakUserResponse update(String keycloakUserId, UpdateKeycloakUserRequest request) {
        User localUser = userRepository.findByKeycloakSubject(keycloakUserId)
                .orElseThrow(() -> new InterviewException(ErrorCode.USER_NOT_FOUND));

        KeycloakAdminClient.KeycloakUser keycloakUser = keycloakClient.updateUser(keycloakUserId, request);
        localUser.syncFromKeycloak(request.username(), request.email(), request.displayName(), request.enabled());

        return new KeycloakUserResponse(
                keycloakUser.id(), keycloakUser.username(), keycloakUser.email(),
                keycloakUser.firstName(), keycloakUser.lastName(), keycloakUser.enabled(),
                keycloakUser.roles(), keycloakUser.attributes(), UserResponse.from(localUser));
    }
}
