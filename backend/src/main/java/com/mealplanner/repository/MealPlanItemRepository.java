package com.mealplanner.repository;

import com.mealplanner.domain.MealPlanItem;
import com.mealplanner.domain.MealType;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealPlanItemRepository extends JpaRepository<MealPlanItem, Long> {

    Optional<MealPlanItem> findByMealPlanIdAndMealDateAndMealType(Long mealPlanId, LocalDate mealDate, MealType mealType);

    boolean existsByRecipeId(Long recipeId);
}
