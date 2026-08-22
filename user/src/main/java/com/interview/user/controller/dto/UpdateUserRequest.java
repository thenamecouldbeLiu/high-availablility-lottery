package com.interview.user.controller.dto;

import com.interview.user.domain.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @NotBlank @Size(max = 100) String username,
        @NotBlank @Email @Size(max = 320) String email,
        @Size(max = 150) String displayName,
        @NotNull UserRole role,
        boolean enabled
) {
}
