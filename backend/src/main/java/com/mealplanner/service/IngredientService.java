package com.mealplanner.service;

import com.mealplanner.domain.Ingredient;
import com.mealplanner.dto.IngredientRequest;
import com.mealplanner.dto.IngredientResponse;
import com.mealplanner.dto.RecipeIngredientRequest;
import com.mealplanner.exception.ResourceNotFoundException;
import com.mealplanner.repository.IngredientRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IngredientService {

    private final IngredientRepository ingredientRepository;

    public IngredientService(IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }

    @Transactional(readOnly = true)
    public List<IngredientResponse> list(String query) {
        String normalizedQuery = normalize(query);
        return ingredientRepository.findAll().stream()
                .filter(ingredient -> normalizedQuery == null || ingredient.getName().toLowerCase().contains(normalizedQuery))
                .sorted(Comparator.comparing(Ingredient::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public IngredientResponse create(IngredientRequest request) {
        Ingredient ingredient = ingredientRepository.findByNameIgnoreCase(request.name().trim())
                .orElseGet(Ingredient::new);
        ingredient.setName(request.name().trim());
        ingredient.setShoppingCategory(request.shoppingCategory().trim());
        ingredient.setDefaultUnit(trimToNull(request.defaultUnit()));
        return toResponse(ingredientRepository.save(ingredient));
    }

    @Transactional
    public Ingredient resolve(RecipeIngredientRequest request) {
        if (request.ingredientId() != null) {
            return ingredientRepository.findById(request.ingredientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ingredient introuvable : " + request.ingredientId()));
        }
        String name = trimToNull(request.ingredientName());
        if (name == null) {
            throw new ResourceNotFoundException("Un ingredient doit avoir un identifiant ou un nom.");
        }
        return ingredientRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> {
                    Ingredient ingredient = new Ingredient();
                    ingredient.setName(name);
                    ingredient.setShoppingCategory(defaultString(request.shoppingCategory(), "Epicerie salee"));
                    ingredient.setDefaultUnit(trimToNull(request.defaultUnit()));
                    return ingredientRepository.save(ingredient);
                });
    }

    public IngredientResponse toResponse(Ingredient ingredient) {
        return new IngredientResponse(
                ingredient.getId(),
                ingredient.getName(),
                ingredient.getShoppingCategory(),
                ingredient.getDefaultUnit()
        );
    }

    private String defaultString(String value, String fallback) {
        String trimmed = trimToNull(value);
        return trimmed == null ? fallback : trimmed;
    }

    private String normalize(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? null : trimmed.toLowerCase();
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
