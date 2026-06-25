package com.mealplanner.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.mealplanner.domain.Ingredient;
import com.mealplanner.domain.MealPlan;
import com.mealplanner.domain.MealPlanItem;
import com.mealplanner.domain.MealType;
import com.mealplanner.domain.Recipe;
import com.mealplanner.domain.RecipeIngredient;
import com.mealplanner.domain.RecipeStep;
import com.mealplanner.dto.ShoppingListItemResponse;
import com.mealplanner.dto.ShoppingListResponse;
import com.mealplanner.repository.IngredientRepository;
import com.mealplanner.repository.MealPlanItemRepository;
import com.mealplanner.repository.MealPlanRepository;
import com.mealplanner.repository.RecipeRepository;
import com.mealplanner.repository.ShoppingListItemRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ShoppingListServiceTest {

    @Autowired
    private ShoppingListService shoppingListService;

    @Autowired
    private ShoppingListItemRepository shoppingListItemRepository;

    @Autowired
    private MealPlanItemRepository mealPlanItemRepository;

    @Autowired
    private MealPlanRepository mealPlanRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @BeforeEach
    void cleanDatabase() {
        shoppingListItemRepository.deleteAll();
        mealPlanItemRepository.deleteAll();
        mealPlanRepository.deleteAll();
        recipeRepository.deleteAll();
        ingredientRepository.deleteAll();
    }

    @Test
    void consolidatesQuantitiesOnlyWhenIngredientAndUnitMatch() {
        Ingredient eggs = ingredient("Oeufs", "Frais", "piece");
        Ingredient pasta = ingredient("Pates", "Epicerie salee", "g");
        ingredientRepository.saveAll(List.of(eggs, pasta));

        Recipe omelette = recipe("Omelette", ingredientLine(eggs, "3", "piece"));
        Recipe carbonara = recipe(
                "Carbonara",
                ingredientLine(eggs, "2", "piece"),
                ingredientLine(pasta, "500", "g")
        );
        recipeRepository.saveAll(List.of(omelette, carbonara));

        LocalDate monday = LocalDate.of(2026, 6, 22);
        MealPlan mealPlan = new MealPlan();
        mealPlan.setWeekStartDate(monday);
        mealPlan.addItem(item(omelette, monday, MealType.LUNCH));
        mealPlan.addItem(item(carbonara, monday.plusDays(1), MealType.DINNER));
        mealPlanRepository.save(mealPlan);

        ShoppingListResponse response = shoppingListService.getShoppingList(monday);

        ShoppingListItemResponse eggsLine = response.categories().get("Frais").stream()
                .filter(line -> line.ingredientName().equals("Oeufs"))
                .findFirst()
                .orElseThrow();
        ShoppingListItemResponse pastaLine = response.categories().get("Epicerie salee").stream()
                .filter(line -> line.ingredientName().equals("Pates"))
                .findFirst()
                .orElseThrow();

        assertThat(eggsLine.quantity()).isEqualByComparingTo("5");
        assertThat(eggsLine.unit()).isEqualTo("piece");
        assertThat(pastaLine.quantity()).isEqualByComparingTo("500");
    }

    private Ingredient ingredient(String name, String shoppingCategory, String unit) {
        Ingredient ingredient = new Ingredient();
        ingredient.setName(name);
        ingredient.setShoppingCategory(shoppingCategory);
        ingredient.setDefaultUnit(unit);
        return ingredient;
    }

    private Recipe recipe(String name, RecipeIngredient... ingredients) {
        Recipe recipe = new Recipe();
        recipe.setName(name);
        recipe.setServings(2);
        recipe.setPreparationTimeMinutes(10);
        recipe.setCookingTimeMinutes(10);
        for (RecipeIngredient ingredient : ingredients) {
            recipe.addIngredient(ingredient);
        }
        RecipeStep step = new RecipeStep();
        step.setStepOrder(1);
        step.setDescription("Preparer la recette.");
        recipe.addStep(step);
        return recipe;
    }

    private RecipeIngredient ingredientLine(Ingredient ingredient, String quantity, String unit) {
        RecipeIngredient recipeIngredient = new RecipeIngredient();
        recipeIngredient.setIngredient(ingredient);
        recipeIngredient.setQuantity(new BigDecimal(quantity));
        recipeIngredient.setUnit(unit);
        return recipeIngredient;
    }

    private MealPlanItem item(Recipe recipe, LocalDate date, MealType mealType) {
        MealPlanItem item = new MealPlanItem();
        item.setRecipe(recipe);
        item.setMealDate(date);
        item.setMealType(mealType);
        return item;
    }
}
