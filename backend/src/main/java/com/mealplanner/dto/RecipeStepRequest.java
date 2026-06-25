package com.mealplanner.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record RecipeStepRequest(
        @NotBlank String description,
        @Min(0) Integer durationMinutes
) {
}
