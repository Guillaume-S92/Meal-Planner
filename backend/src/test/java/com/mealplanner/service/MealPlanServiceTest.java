package com.mealplanner.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.mealplanner.domain.Ingredient;
import com.mealplanner.domain.MealPlan;
import com.mealplanner.domain.MealPlanItem;
import com.mealplanner.domain.MealType;
import com.mealplanner.domain.Recipe;
import com.mealplanner.domain.RecipeIngredient;
import com.mealplanner.domain.RecipeStep;
import com.mealplanner.dto.MealPlanResponse;
import com.mealplanner.repository.IngredientRepository;
import com.mealplanner.repository.MealPlanItemRepository;
import com.mealplanner.repository.MealPlanRepository;
import com.mealplanner.repository.RecipeRepository;
import com.mealplanner.repository.ShoppingListItemRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class MealPlanServiceTest {

    @Autowired
    private MealPlanService mealPlanService;

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
    void removeItemDeletesMealAndReturnsFreshWeek() {
        Ingredient pasta = new Ingredient();
        pasta.setName("Pates");
        pasta.setShoppingCategory("Epicerie salee");
        pasta.setDefaultUnit("g");
        ingredientRepository.save(pasta);

        Recipe recipe = new Recipe();
        recipe.setName("Pates tomate");
        recipe.setServings(2);
        recipe.setPreparationTimeMinutes(5);
        recipe.setCookingTimeMinutes(12);

        RecipeIngredient ingredient = new RecipeIngredient();
        ingredient.setIngredient(pasta);
        ingredient.setQuantity(new BigDecimal("250"));
        ingredient.setUnit("g");
        recipe.addIngredient(ingredient);

        RecipeStep step = new RecipeStep();
        step.setStepOrder(1);
        step.setDescription("Preparer la recette.");
        recipe.addStep(step);
        recipeRepository.save(recipe);

        LocalDate monday = LocalDate.of(2026, 6, 22);
        MealPlan mealPlan = new MealPlan();
        mealPlan.setWeekStartDate(monday);
        MealPlanItem item = new MealPlanItem();
        item.setRecipe(recipe);
        item.setMealDate(monday);
        item.setMealType(MealType.DINNER);
        mealPlan.addItem(item);
        MealPlan savedPlan = mealPlanRepository.save(mealPlan);
        Long itemId = savedPlan.getItems().get(0).getId();

        MealPlanResponse response = mealPlanService.removeItem(monday, itemId);

        assertThat(response.items()).isEmpty();
        assertThat(mealPlanItemRepository.findById(itemId)).isEmpty();
        assertThat(mealPlanService.getWeek(monday).items()).isEmpty();
    }
}
