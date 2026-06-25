package com.mealplanner.config;

import com.mealplanner.domain.Ingredient;
import com.mealplanner.domain.MealPlan;
import com.mealplanner.domain.MealPlanItem;
import com.mealplanner.domain.MealType;
import com.mealplanner.domain.Recipe;
import com.mealplanner.domain.RecipeIngredient;
import com.mealplanner.domain.RecipeStep;
import com.mealplanner.repository.IngredientRepository;
import com.mealplanner.repository.MealPlanRepository;
import com.mealplanner.repository.RecipeRepository;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class DataInitializer {

    @Bean
    @Transactional
    @ConditionalOnProperty(name = "meal-planner.seed.enabled", havingValue = "true", matchIfMissing = true)
    CommandLineRunner seedData(
            RecipeRepository recipeRepository,
            IngredientRepository ingredientRepository,
            MealPlanRepository mealPlanRepository
    ) {
        return args -> {
            if (recipeRepository.count() > 0) {
                return;
            }

            Ingredient eggs = ingredientRepository.save(ingredient("Oeufs", "Frais", "piece"));
            Ingredient pasta = ingredientRepository.save(ingredient("Pates", "Epicerie salee", "g"));
            Ingredient lardons = ingredientRepository.save(ingredient("Lardons", "Viandes et poissons", "g"));
            Ingredient parmesan = ingredientRepository.save(ingredient("Parmesan", "Frais", "g"));
            Ingredient chicken = ingredientRepository.save(ingredient("Poulet", "Viandes et poissons", "g"));
            Ingredient rice = ingredientRepository.save(ingredient("Riz", "Epicerie salee", "g"));
            Ingredient coconutMilk = ingredientRepository.save(ingredient("Lait de coco", "Epicerie salee", "ml"));
            Ingredient chickpeas = ingredientRepository.save(ingredient("Pois chiches", "Epicerie salee", "g"));
            Ingredient tomatoes = ingredientRepository.save(ingredient("Tomates", "Fruits et legumes", "piece"));
            Ingredient cucumber = ingredientRepository.save(ingredient("Concombre", "Fruits et legumes", "piece"));

            Recipe carbonara = recipeRepository.save(recipe(
                    "Pates carbonara",
                    "Une recette rapide pour les soirs de semaine.",
                    4,
                    10,
                    15,
                    "Plats principaux",
                    "https://images.unsplash.com/photo-1621996346565-e3dbc646d9a9?auto=format&fit=crop&w=900&q=80",
                    Set.of("pates", "facile", "moins de 30 minutes"),
                    new RecipeIngredientData(pasta, "400", "g"),
                    new RecipeIngredientData(lardons, "200", "g"),
                    new RecipeIngredientData(eggs, "3", "piece"),
                    new RecipeIngredientData(parmesan, "80", "g")
            ));
            addSteps(carbonara,
                    "Faire cuire les pates dans une grande casserole d'eau bouillante salee.",
                    "Faire revenir les lardons pendant la cuisson des pates.",
                    "Melanger les oeufs avec le parmesan rape.",
                    "Ajouter les pates chaudes et melanger rapidement hors du feu.");

            Recipe curry = recipeRepository.save(recipe(
                    "Poulet curry coco",
                    "Un plat complet, doux et parfume.",
                    4,
                    15,
                    25,
                    "Plats principaux",
                    "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?auto=format&fit=crop&w=900&q=80",
                    Set.of("poulet", "riz", "batch cooking"),
                    new RecipeIngredientData(chicken, "500", "g"),
                    new RecipeIngredientData(rice, "300", "g"),
                    new RecipeIngredientData(coconutMilk, "400", "ml")
            ));
            addSteps(curry,
                    "Couper le poulet en morceaux reguliers.",
                    "Faire dorer le poulet avec une cuillere d'huile.",
                    "Ajouter le curry puis le lait de coco.",
                    "Laisser mijoter et servir avec le riz.");

            Recipe salad = recipeRepository.save(recipe(
                    "Salade pois chiches tomate",
                    "Fraiche, economique et facile a preparer.",
                    2,
                    12,
                    0,
                    "Vegetarien",
                    "https://images.unsplash.com/photo-1540420773420-3366772f4999?auto=format&fit=crop&w=900&q=80",
                    Set.of("healthy", "economique", "vegetarien"),
                    new RecipeIngredientData(chickpeas, "300", "g"),
                    new RecipeIngredientData(tomatoes, "4", "piece"),
                    new RecipeIngredientData(cucumber, "1", "piece")
            ));
            addSteps(salad,
                    "Rincer et egoutter les pois chiches.",
                    "Couper les tomates et le concombre.",
                    "Melanger avec une vinaigrette simple et servir frais.");

            recipeRepository.save(carbonara);
            recipeRepository.save(curry);
            recipeRepository.save(salad);

            LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            MealPlan mealPlan = new MealPlan();
            mealPlan.setWeekStartDate(monday);
            mealPlan.addItem(item(mealPlan, carbonara, monday, MealType.DINNER));
            mealPlan.addItem(item(mealPlan, salad, monday.plusDays(1), MealType.LUNCH));
            mealPlan.addItem(item(mealPlan, curry, monday.plusDays(2), MealType.DINNER));
            mealPlanRepository.save(mealPlan);
        };
    }

    private Ingredient ingredient(String name, String category, String defaultUnit) {
        Ingredient ingredient = new Ingredient();
        ingredient.setName(name);
        ingredient.setShoppingCategory(category);
        ingredient.setDefaultUnit(defaultUnit);
        return ingredient;
    }

    private Recipe recipe(
            String name,
            String description,
            int servings,
            int preparationTime,
            int cookingTime,
            String category,
            String imageUrl,
            Set<String> tags,
            RecipeIngredientData... ingredientData
    ) {
        Recipe recipe = new Recipe();
        recipe.setName(name);
        recipe.setDescription(description);
        recipe.setServings(servings);
        recipe.setPreparationTimeMinutes(preparationTime);
        recipe.setCookingTimeMinutes(cookingTime);
        recipe.setCategory(category);
        recipe.setImageUrl(imageUrl);
        recipe.setTags(tags);
        for (RecipeIngredientData data : ingredientData) {
            RecipeIngredient recipeIngredient = new RecipeIngredient();
            recipeIngredient.setIngredient(data.ingredient());
            recipeIngredient.setQuantity(new BigDecimal(data.quantity()));
            recipeIngredient.setUnit(data.unit());
            recipe.addIngredient(recipeIngredient);
        }
        return recipe;
    }

    private void addSteps(Recipe recipe, String... descriptions) {
        for (int index = 0; index < descriptions.length; index++) {
            RecipeStep step = new RecipeStep();
            step.setStepOrder(index + 1);
            step.setDescription(descriptions[index]);
            recipe.addStep(step);
        }
    }

    private MealPlanItem item(MealPlan mealPlan, Recipe recipe, LocalDate date, MealType mealType) {
        MealPlanItem item = new MealPlanItem();
        item.setMealPlan(mealPlan);
        item.setRecipe(recipe);
        item.setMealDate(date);
        item.setMealType(mealType);
        return item;
    }

    private record RecipeIngredientData(Ingredient ingredient, String quantity, String unit) {
    }
}
