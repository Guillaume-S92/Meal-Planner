package com.mealplanner.dto;

public record AdminUserUpdateRequest(
        Boolean enabled,
        String role,
        String password
) {
}
