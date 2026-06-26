package com.mealplanner.dto;

public record CurrentUserResponse(
        Long id,
        String username,
        String role
) {
}
