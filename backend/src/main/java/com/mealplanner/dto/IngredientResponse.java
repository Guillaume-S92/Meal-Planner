package com.mealplanner.dto;

public record IngredientResponse(
        Long id,
        String name,
        String shoppingCategory,
        String defaultUnit
) {
}
