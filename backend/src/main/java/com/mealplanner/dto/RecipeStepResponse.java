package com.mealplanner.dto;

public record RecipeStepResponse(
        Long id,
        Integer stepOrder,
        String description,
        Integer durationMinutes
) {
}
