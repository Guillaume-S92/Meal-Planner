package com.mealplanner.service;

import com.mealplanner.domain.Ingredient;
import com.mealplanner.domain.MealPlan;
import com.mealplanner.domain.MealPlanItem;
import com.mealplanner.domain.RecipeIngredient;
import com.mealplanner.domain.ShoppingListItem;
import com.mealplanner.dto.ShoppingListItemResponse;
import com.mealplanner.dto.ShoppingListItemStateRequest;
import com.mealplanner.dto.ShoppingListResponse;
import com.mealplanner.exception.ResourceNotFoundException;
import com.mealplanner.repository.ShoppingListItemRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShoppingListService {

    private final MealPlanService mealPlanService;
    private final ShoppingListItemRepository shoppingListItemRepository;

    public ShoppingListService(MealPlanService mealPlanService, ShoppingListItemRepository shoppingListItemRepository) {
        this.mealPlanService = mealPlanService;
        this.shoppingListItemRepository = shoppingListItemRepository;
    }

    @Transactional
    public ShoppingListResponse getShoppingList(LocalDate dateInWeek) {
        MealPlan mealPlan = mealPlanService.getOrCreateWeek(dateInWeek);
        List<ShoppingListItem> generatedItems = regenerateItems(mealPlan);
        return toResponse(mealPlan.getWeekStartDate(), generatedItems);
    }

    @Transactional
    public ShoppingListResponse updateItem(LocalDate dateInWeek, Long itemId, ShoppingListItemStateRequest request) {
        MealPlan mealPlan = mealPlanService.getOrCreateWeek(dateInWeek);
        ShoppingListItem item = shoppingListItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Element de liste de courses introuvable : " + itemId));
        if (!item.getMealPlan().getId().equals(mealPlan.getId())) {
            throw new ResourceNotFoundException("Element de liste de courses introuvable pour cette semaine : " + itemId);
        }
        item.setChecked(request.checked());
        item.setExcluded(request.excluded());
        shoppingListItemRepository.save(item);
        return getShoppingList(mealPlan.getWeekStartDate());
    }

    private List<ShoppingListItem> regenerateItems(MealPlan mealPlan) {
        Map<ShoppingKey, ShoppingAggregate> aggregates = new LinkedHashMap<>();
        for (MealPlanItem planItem : mealPlan.getItems()) {
            for (RecipeIngredient recipeIngredient : planItem.getRecipe().getIngredients()) {
                Ingredient ingredient = recipeIngredient.getIngredient();
                ShoppingKey key = new ShoppingKey(ingredient.getId(), normalizeUnit(recipeIngredient.getUnit()));
                aggregates.computeIfAbsent(key, ignored -> new ShoppingAggregate(
                                ingredient,
                                recipeIngredient.getUnit().trim(),
                                ingredient.getShoppingCategory(),
                                BigDecimal.ZERO
                        ))
                        .add(recipeIngredient.getQuantity());
            }
        }

        Map<ShoppingKey, ShoppingListItem> existingItems = shoppingListItemRepository.findByMealPlanId(mealPlan.getId()).stream()
                .collect(Collectors.toMap(
                        item -> new ShoppingKey(item.getIngredient().getId(), normalizeUnit(item.getUnit())),
                        Function.identity()
                ));

        List<Long> activeIds = new ArrayList<>();
        List<ShoppingListItem> savedItems = new ArrayList<>();
        for (Map.Entry<ShoppingKey, ShoppingAggregate> entry : aggregates.entrySet()) {
            ShoppingAggregate aggregate = entry.getValue();
            ShoppingListItem item = existingItems.getOrDefault(entry.getKey(), new ShoppingListItem());
            item.setMealPlan(mealPlan);
            item.setIngredient(aggregate.ingredient());
            item.setQuantity(aggregate.quantity());
            item.setUnit(aggregate.unit());
            item.setShoppingCategory(aggregate.shoppingCategory());
            ShoppingListItem saved = shoppingListItemRepository.save(item);
            activeIds.add(saved.getId());
            savedItems.add(saved);
        }

        deleteObsoleteItems(mealPlan.getId(), activeIds);
        return savedItems.stream()
                .sorted(Comparator.comparing(ShoppingListItem::getShoppingCategory, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(item -> item.getIngredient().getName(), String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private void deleteObsoleteItems(Long mealPlanId, Collection<Long> activeIds) {
        if (activeIds.isEmpty()) {
            shoppingListItemRepository.deleteByMealPlanId(mealPlanId);
        } else {
            shoppingListItemRepository.deleteByMealPlanIdAndIdNotIn(mealPlanId, activeIds);
        }
    }

    private ShoppingListResponse toResponse(LocalDate weekStart, List<ShoppingListItem> items) {
        Map<String, List<ShoppingListItemResponse>> categories = items.stream()
                .map(this::toItemResponse)
                .collect(Collectors.groupingBy(
                        ShoppingListItemResponse::shoppingCategory,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
        return new ShoppingListResponse(weekStart, categories);
    }

    private ShoppingListItemResponse toItemResponse(ShoppingListItem item) {
        return new ShoppingListItemResponse(
                item.getId(),
                item.getIngredient().getId(),
                item.getIngredient().getName(),
                item.getQuantity(),
                item.getUnit(),
                item.getShoppingCategory(),
                item.isChecked(),
                item.isExcluded()
        );
    }

    private String normalizeUnit(String unit) {
        return Objects.requireNonNullElse(unit, "").trim().toLowerCase();
    }

    private record ShoppingKey(Long ingredientId, String unit) {
    }

    private static final class ShoppingAggregate {
        private final Ingredient ingredient;
        private final String unit;
        private final String shoppingCategory;
        private BigDecimal quantity;

        private ShoppingAggregate(Ingredient ingredient, String unit, String shoppingCategory, BigDecimal quantity) {
            this.ingredient = ingredient;
            this.unit = unit;
            this.shoppingCategory = shoppingCategory;
            this.quantity = quantity;
        }

        private void add(BigDecimal value) {
            quantity = quantity.add(value);
        }

        private Ingredient ingredient() {
            return ingredient;
        }

        private String unit() {
            return unit;
        }

        private String shoppingCategory() {
            return shoppingCategory;
        }

        private BigDecimal quantity() {
            return quantity.stripTrailingZeros();
        }
    }
}
