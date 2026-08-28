package com.interview.user.controller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record UpdateKeycloakUserRequest(
        @NotBlank @Size(max = 100) String username,
        @NotBlank @Email @Size(max = 320) String email,
        @Size(max = 100) String firstName,
        @Size(max = 100) String lastName,
        @Size(max = 150) String displayName,
        boolean enabled,
        @NotEmpty Set<@Pattern(regexp = "ADMIN|NORMAL_USER") String> roles,
        Map<@NotBlank String, @Valid List<@NotBlank String>> attributes
) {
}
