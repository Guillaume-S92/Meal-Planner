package com.mealplanner.dto;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public record RecipeResponse(
        Long id,
        String name,
        String description,
        Integer servings,
        Integer preparationTimeMinutes,
        Integer cookingTimeMinutes,
        Integer totalTimeMinutes,
        String imageUrl,
        String category,
        Set<String> tags,
        List<RecipeIngredientResponse> ingredients,
        List<RecipeStepResponse> steps,
        Instant createdAt,
        Instant updatedAt
) {
}
