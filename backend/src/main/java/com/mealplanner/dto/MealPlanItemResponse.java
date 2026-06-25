package com.mealplanner.dto;

import com.mealplanner.domain.MealType;
import java.time.LocalDate;

public record MealPlanItemResponse(
        Long id,
        LocalDate date,
        MealType mealType,
        Long recipeId,
        String recipeName,
        String recipeCategory
) {
}
