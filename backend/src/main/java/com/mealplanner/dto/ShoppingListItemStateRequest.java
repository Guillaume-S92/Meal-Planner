package com.mealplanner.dto;

public record ShoppingListItemStateRequest(
        boolean checked,
        boolean excluded
) {
}
