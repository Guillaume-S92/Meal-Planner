package com.mealplanner.dto;

import java.math.BigDecimal;

public record ShoppingListItemResponse(
        Long id,
        Long ingredientId,
        String ingredientName,
        BigDecimal quantity,
        String unit,
        String shoppingCategory,
        boolean checked,
        boolean excluded
) {
}
