package com.mealplanner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record RecipeIngredientRequest(
        Long ingredientId,
        String ingredientName,
        String shoppingCategory,
        String defaultUnit,
        @NotNull @Positive BigDecimal quantity,
        @NotBlank String unit,
        boolean optional,
        String comment
) {
}
