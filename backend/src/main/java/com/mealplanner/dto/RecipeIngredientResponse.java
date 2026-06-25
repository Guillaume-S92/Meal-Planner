package com.mealplanner.dto;

import java.math.BigDecimal;

public record RecipeIngredientResponse(
        Long id,
        Long ingredientId,
        String ingredientName,
        String shoppingCategory,
        BigDecimal quantity,
        String unit,
        boolean optional,
        String comment
) {
}
