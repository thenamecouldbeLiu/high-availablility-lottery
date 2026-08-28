package com.interview.user.service;

import com.interview.common.exception.ErrorCode;
import com.interview.common.exception.InterviewException;
import com.interview.user.controller.dto.CreateUserRequest;
import com.interview.user.controller.dto.UpdateUserRequest;
import com.interview.user.controller.dto.UserResponse;
import com.interview.user.domain.User;
import com.interview.user.domain.UserRole;
import com.interview.user.infra.security.AuthenticatedUser;
import com.interview.user.repository.UserRepository;
import com.interview.user.utils.UserUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserResponse create(CreateUserRequest request) {
        User user = new User(request.keycloakSubject(), request.username(), request.email(),
                request.displayName(), request.roles(), request.enabled());
        return UserResponse.from(repository.save(user));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'NORMAL_USER')")
    public UserResponse get(UUID id) {
        User user = find(id);
        assertCanRead(user);
        return UserResponse.from(user);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'NORMAL_USER')")
    @Transactional
    public UserResponse getOrCreateCurrentUser() {
        AuthenticatedUser currentUser = UserUtil.requireCurrentUser();
        return repository.findByKeycloakSubject(currentUser.subject())
                .map(UserResponse::from)
                .orElseGet(() -> UserResponse.from(repository.save(new User(
                        currentUser.subject(),
                        currentUser.username(),
                        currentUser.email(),
                        currentUser.username(),
                        currentUser.roles().stream()
                                .map(UserRole::valueOf)
                                .collect(Collectors.toUnmodifiableSet()),
                        true))));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserResponse> search(String keyword, Pageable pageable) {
        String normalized = keyword == null || keyword.isBlank() ? null : keyword.trim();
        return repository.search(normalized, pageable).map(UserResponse::from);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest request) {
        User user = find(id);
        user.update(request.username(), request.email(), request.displayName(),
                request.roles(), request.enabled());
        return UserResponse.from(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void delete(UUID id) {
        repository.delete(find(id));
    }

    private User find(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new InterviewException(ErrorCode.USER_NOT_FOUND));
    }

    private void assertCanRead(User user) {
        if (!UserUtil.isAdmin() && !user.getKeycloakSubject().equals(UserUtil.currentSubject())) {
            throw new InterviewException(ErrorCode.ACCESS_DENIED);
        }
    }
}
