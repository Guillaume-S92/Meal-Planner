package com.mealplanner.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Set;

public record RecipeRequest(
        @NotBlank String name,
        String description,
        @Min(1) Integer servings,
        @Min(0) Integer preparationTimeMinutes,
        @Min(0) Integer cookingTimeMinutes,
        String imageUrl,
        String category,
        Set<String> tags,
        @NotEmpty @Valid List<RecipeIngredientRequest> ingredients,
        @NotEmpty @Valid List<RecipeStepRequest> steps
) {
}
