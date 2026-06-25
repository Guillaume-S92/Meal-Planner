package com.mealplanner.dto;

import java.time.LocalDate;
import java.util.List;

public record MealPlanResponse(
        Long id,
        LocalDate weekStartDate,
        List<MealPlanItemResponse> items
) {
}
