package com.mealplanner.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record ShoppingListResponse(
        LocalDate weekStartDate,
        Map<String, List<ShoppingListItemResponse>> categories
) {
}
