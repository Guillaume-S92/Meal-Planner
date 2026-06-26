package com.mealplanner.dto;

import java.time.Instant;

public record AdminUserResponse(
        Long id,
        String username,
        String role,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
}
