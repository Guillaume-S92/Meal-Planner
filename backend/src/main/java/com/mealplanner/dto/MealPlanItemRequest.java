package com.mealplanner.dto;

import com.mealplanner.domain.MealType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record MealPlanItemRequest(
        @NotNull LocalDate date,
        @NotNull MealType mealType,
        @NotNull Long recipeId
) {
}
