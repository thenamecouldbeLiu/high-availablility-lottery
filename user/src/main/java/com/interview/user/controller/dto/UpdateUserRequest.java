package com.interview.user.controller.dto;

import com.interview.user.domain.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UpdateUserRequest(
        @NotBlank @Size(max = 100) String username,
        @NotBlank @Email @Size(max = 320) String email,
        @Size(max = 150) String displayName,
        @NotEmpty Set<UserRole> roles,
        boolean enabled
) {
}
