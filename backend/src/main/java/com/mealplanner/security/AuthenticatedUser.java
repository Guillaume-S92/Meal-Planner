package com.mealplanner.security;

public record AuthenticatedUser(
        Long id,
        String username,
        String role
) {
}
