package com.mealplanner.repository;

import com.mealplanner.domain.MealPlan;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {

    Optional<MealPlan> findByWeekStartDate(LocalDate weekStartDate);
}
