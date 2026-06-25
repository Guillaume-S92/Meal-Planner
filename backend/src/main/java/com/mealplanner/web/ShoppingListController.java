package com.mealplanner.web;

import com.mealplanner.dto.ShoppingListItemStateRequest;
import com.mealplanner.dto.ShoppingListResponse;
import com.mealplanner.service.ShoppingListService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shopping-lists")
public class ShoppingListController {

    private final ShoppingListService shoppingListService;

    public ShoppingListController(ShoppingListService shoppingListService) {
        this.shoppingListService = shoppingListService;
    }

    @GetMapping("/{week}")
    public ShoppingListResponse getShoppingList(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate week) {
        return shoppingListService.getShoppingList(week);
    }

    @PatchMapping("/{week}/items/{itemId}")
    public ShoppingListResponse updateItem(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate week,
            @PathVariable Long itemId,
            @RequestBody ShoppingListItemStateRequest request
    ) {
        return shoppingListService.updateItem(week, itemId, request);
    }
}
