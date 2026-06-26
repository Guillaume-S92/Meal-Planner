package com.mealplanner.repository;

import com.mealplanner.domain.Recipe;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    Optional<Recipe> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
