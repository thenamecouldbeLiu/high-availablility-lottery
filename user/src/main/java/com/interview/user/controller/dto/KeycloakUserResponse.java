package com.interview.user.controller.dto;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record KeycloakUserResponse(
        String id,
        String username,
        String email,
        String firstName,
        String lastName,
        boolean enabled,
        Set<String> roles,
        Map<String, List<String>> attributes,
        UserResponse localUser
) {
}
