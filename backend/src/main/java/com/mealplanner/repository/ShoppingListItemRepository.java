package com.mealplanner.repository;

import com.mealplanner.domain.ShoppingListItem;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShoppingListItemRepository extends JpaRepository<ShoppingListItem, Long> {

    List<ShoppingListItem> findByMealPlanId(Long mealPlanId);

    void deleteByMealPlanId(Long mealPlanId);

    void deleteByMealPlanIdAndIdNotIn(Long mealPlanId, Collection<Long> ids);
}
