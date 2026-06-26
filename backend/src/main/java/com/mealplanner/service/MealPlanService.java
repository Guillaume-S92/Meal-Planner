package com.mealplanner.service;

import com.mealplanner.domain.MealPlan;
import com.mealplanner.domain.MealPlanItem;
import com.mealplanner.domain.Recipe;
import com.mealplanner.dto.MealPlanItemRequest;
import com.mealplanner.dto.MealPlanItemResponse;
import com.mealplanner.dto.MealPlanResponse;
import com.mealplanner.exception.BusinessRuleException;
import com.mealplanner.exception.ResourceNotFoundException;
import com.mealplanner.repository.MealPlanItemRepository;
import com.mealplanner.repository.MealPlanRepository;
import com.mealplanner.repository.RecipeRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MealPlanService {

    private final MealPlanRepository mealPlanRepository;
    private final MealPlanItemRepository mealPlanItemRepository;
    private final RecipeRepository recipeRepository;

    public MealPlanService(
            MealPlanRepository mealPlanRepository,
            MealPlanItemRepository mealPlanItemRepository,
            RecipeRepository recipeRepository
    ) {
        this.mealPlanRepository = mealPlanRepository;
        this.mealPlanItemRepository = mealPlanItemRepository;
        this.recipeRepository = recipeRepository;
    }

    @Transactional(readOnly = true)
    public MealPlanResponse getWeek(LocalDate dateInWeek) {
        MealPlan mealPlan = getOrCreateWeek(dateInWeek);
        return toResponse(mealPlan);
    }

    @Transactional
    public MealPlanResponse addOrReplaceItem(LocalDate dateInWeek, MealPlanItemRequest request) {
        LocalDate weekStart = startOfWeek(dateInWeek);
        assertDateBelongsToWeek(request.date(), weekStart);
        MealPlan mealPlan = getOrCreateWeek(weekStart);
        Recipe recipe = recipeRepository.findById(request.recipeId())
                .orElseThrow(() -> new ResourceNotFoundException("Recette introuvable : " + request.recipeId()));

        MealPlanItem item = mealPlanItemRepository
                .findByMealPlanIdAndMealDateAndMealType(mealPlan.getId(), request.date(), request.mealType())
                .orElseGet(MealPlanItem::new);
        item.setMealPlan(mealPlan);
        item.setMealDate(request.date());
        item.setMealType(request.mealType());
        item.setRecipe(recipe);
        mealPlanItemRepository.save(item);
        return toResponse(getOrCreateWeek(weekStart));
    }

    @Transactional
    public MealPlanResponse removeItem(LocalDate dateInWeek, Long itemId) {
        LocalDate weekStart = startOfWeek(dateInWeek);
        MealPlan mealPlan = getOrCreateWeek(weekStart);
        MealPlanItem item = mealPlanItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Element de planning introuvable : " + itemId));
        if (!item.getMealPlan().getId().equals(mealPlan.getId())) {
            throw new ResourceNotFoundException("Element de planning introuvable pour cette semaine : " + itemId);
        }
        mealPlan.removeItem(item);
        mealPlanItemRepository.delete(item);
        mealPlanItemRepository.flush();
        return getWeek(weekStart);
    }

    @Transactional
    public MealPlan getOrCreateWeek(LocalDate dateInWeek) {
        LocalDate weekStart = startOfWeek(dateInWeek);
        return mealPlanRepository.findByWeekStartDate(weekStart)
                .orElseGet(() -> {
                    MealPlan mealPlan = new MealPlan();
                    mealPlan.setWeekStartDate(weekStart);
                    return mealPlanRepository.save(mealPlan);
                });
    }

    public LocalDate startOfWeek(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private void assertDateBelongsToWeek(LocalDate date, LocalDate weekStart) {
        if (date.isBefore(weekStart) || date.isAfter(weekStart.plusDays(6))) {
            throw new BusinessRuleException("La date du repas doit appartenir a la semaine demandee.");
        }
    }

    private MealPlanResponse toResponse(MealPlan mealPlan) {
        return new MealPlanResponse(
                mealPlan.getId(),
                mealPlan.getWeekStartDate(),
                mealPlan.getItems().stream()
                        .sorted(Comparator.comparing(MealPlanItem::getMealDate).thenComparing(MealPlanItem::getMealType))
                        .map(this::toItemResponse)
                        .toList()
        );
    }

    private MealPlanItemResponse toItemResponse(MealPlanItem item) {
        return new MealPlanItemResponse(
                item.getId(),
                item.getMealDate(),
                item.getMealType(),
                item.getRecipe().getId(),
                item.getRecipe().getName(),
                item.getRecipe().getCategory()
        );
    }
}
