package com.mealplanner.dto;

public record AuthResponse(
        String token,
        CurrentUserResponse user
) {
}
