package com.mealplanner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminUserCreateRequest(
        @NotBlank String username,
        @NotBlank @Size(min = 8) String password,
        String role,
        Boolean enabled
) {
}
