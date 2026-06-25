package com.mealplanner.dto;

import jakarta.validation.constraints.NotBlank;

public record IngredientRequest(
        @NotBlank String name,
        @NotBlank String shoppingCategory,
        String defaultUnit
) {
}
