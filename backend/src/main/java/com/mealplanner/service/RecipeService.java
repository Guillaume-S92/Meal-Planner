package com.mealplanner.service;

import com.mealplanner.domain.Ingredient;
import com.mealplanner.domain.Recipe;
import com.mealplanner.domain.RecipeIngredient;
import com.mealplanner.domain.RecipeStep;
import com.mealplanner.dto.RecipeIngredientRequest;
import com.mealplanner.dto.RecipeIngredientResponse;
import com.mealplanner.dto.RecipeRequest;
import com.mealplanner.dto.RecipeResponse;
import com.mealplanner.dto.RecipeStepRequest;
import com.mealplanner.dto.RecipeStepResponse;
import com.mealplanner.exception.BusinessRuleException;
import com.mealplanner.exception.ResourceNotFoundException;
import com.mealplanner.repository.MealPlanItemRepository;
import com.mealplanner.repository.RecipeRepository;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final MealPlanItemRepository mealPlanItemRepository;
    private final IngredientService ingredientService;

    public RecipeService(
            RecipeRepository recipeRepository,
            MealPlanItemRepository mealPlanItemRepository,
            IngredientService ingredientService
    ) {
        this.recipeRepository = recipeRepository;
        this.mealPlanItemRepository = mealPlanItemRepository;
        this.ingredientService = ingredientService;
    }

    @Transactional(readOnly = true)
    public List<RecipeResponse> search(String query, String category, String tag) {
        String normalizedQuery = normalize(query);
        String normalizedCategory = normalize(category);
        String normalizedTag = normalize(tag);
        return recipeRepository.findAll().stream()
                .filter(recipe -> normalizedQuery == null || recipe.getName().toLowerCase(Locale.ROOT).contains(normalizedQuery))
                .filter(recipe -> normalizedCategory == null || normalizedCategory.equals(normalize(recipe.getCategory())))
                .filter(recipe -> normalizedTag == null || recipe.getTags().stream().map(this::normalize).anyMatch(normalizedTag::equals))
                .sorted(Comparator.comparing(Recipe::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RecipeResponse get(Long id) {
        return toResponse(getRecipe(id));
    }

    @Transactional
    public RecipeResponse create(RecipeRequest request) {
        Recipe recipe = new Recipe();
        applyRequest(recipe, request);
        return toResponse(recipeRepository.save(recipe));
    }

    @Transactional
    public RecipeResponse update(Long id, RecipeRequest request) {
        Recipe recipe = getRecipe(id);
        applyRequest(recipe, request);
        return toResponse(recipeRepository.save(recipe));
    }

    @Transactional
    public void delete(Long id) {
        Recipe recipe = getRecipe(id);
        if (mealPlanItemRepository.existsByRecipeId(id)) {
            throw new BusinessRuleException("Cette recette est utilisee dans un planning et ne peut pas etre supprimee.");
        }
        recipeRepository.delete(recipe);
    }

    private Recipe getRecipe(Long id) {
        return recipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recette introuvable : " + id));
    }

    private void applyRequest(Recipe recipe, RecipeRequest request) {
        recipe.setName(request.name().trim());
        recipe.setDescription(trimToNull(request.description()));
        recipe.setServings(request.servings() == null ? 1 : request.servings());
        recipe.setPreparationTimeMinutes(request.preparationTimeMinutes() == null ? 0 : request.preparationTimeMinutes());
        recipe.setCookingTimeMinutes(request.cookingTimeMinutes() == null ? 0 : request.cookingTimeMinutes());
        recipe.setImageUrl(trimToNull(request.imageUrl()));
        recipe.setCategory(trimToNull(request.category()));
        recipe.setTags(cleanTags(request.tags()));

        recipe.clearIngredients();
        for (RecipeIngredientRequest ingredientRequest : request.ingredients()) {
            Ingredient ingredient = ingredientService.resolve(ingredientRequest);
            RecipeIngredient recipeIngredient = new RecipeIngredient();
            recipeIngredient.setIngredient(ingredient);
            recipeIngredient.setQuantity(ingredientRequest.quantity());
            recipeIngredient.setUnit(ingredientRequest.unit().trim());
            recipeIngredient.setOptional(ingredientRequest.optional());
            recipeIngredient.setComment(trimToNull(ingredientRequest.comment()));
            recipe.addIngredient(recipeIngredient);
        }

        recipe.clearSteps();
        int stepOrder = 1;
        for (RecipeStepRequest stepRequest : request.steps()) {
            RecipeStep step = new RecipeStep();
            step.setStepOrder(stepOrder++);
            step.setDescription(stepRequest.description().trim());
            step.setDurationMinutes(stepRequest.durationMinutes());
            recipe.addStep(step);
        }
    }

    private Set<String> cleanTags(Set<String> tags) {
        if (tags == null) {
            return new LinkedHashSet<>();
        }
        LinkedHashSet<String> cleaned = new LinkedHashSet<>();
        for (String tag : tags) {
            String value = trimToNull(tag);
            if (value != null) {
                cleaned.add(value);
            }
        }
        return cleaned;
    }

    private RecipeResponse toResponse(Recipe recipe) {
        Integer prep = recipe.getPreparationTimeMinutes();
        Integer cook = recipe.getCookingTimeMinutes();
        return new RecipeResponse(
                recipe.getId(),
                recipe.getName(),
                recipe.getDescription(),
                recipe.getServings(),
                prep,
                cook,
                (prep == null ? 0 : prep) + (cook == null ? 0 : cook),
                recipe.getImageUrl(),
                recipe.getCategory(),
                new LinkedHashSet<>(recipe.getTags()),
                recipe.getIngredients().stream().map(this::toIngredientResponse).toList(),
                recipe.getSteps().stream()
                        .sorted(Comparator.comparing(RecipeStep::getStepOrder))
                        .map(this::toStepResponse)
                        .toList(),
                recipe.getCreatedAt(),
                recipe.getUpdatedAt()
        );
    }

    private RecipeIngredientResponse toIngredientResponse(RecipeIngredient recipeIngredient) {
        Ingredient ingredient = recipeIngredient.getIngredient();
        return new RecipeIngredientResponse(
                recipeIngredient.getId(),
                ingredient.getId(),
                ingredient.getName(),
                ingredient.getShoppingCategory(),
                recipeIngredient.getQuantity(),
                recipeIngredient.getUnit(),
                recipeIngredient.isOptional(),
                recipeIngredient.getComment()
        );
    }

    private RecipeStepResponse toStepResponse(RecipeStep step) {
        return new RecipeStepResponse(
                step.getId(),
                step.getStepOrder(),
                step.getDescription(),
                step.getDurationMinutes()
        );
    }

    private String normalize(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? null : trimmed.toLowerCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
