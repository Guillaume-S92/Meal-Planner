package com.mealplanner.web;

import com.mealplanner.dto.MealPlanItemRequest;
import com.mealplanner.dto.MealPlanResponse;
import com.mealplanner.service.MealPlanService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/meal-plans")
public class MealPlanController {

    private final MealPlanService mealPlanService;

    public MealPlanController(MealPlanService mealPlanService) {
        this.mealPlanService = mealPlanService;
    }

    @GetMapping("/{week}")
    public MealPlanResponse getWeek(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate week) {
        return mealPlanService.getWeek(week);
    }

    @PostMapping("/{week}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public MealPlanResponse addOrReplaceItem(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate week,
            @Valid @RequestBody MealPlanItemRequest request
    ) {
        return mealPlanService.addOrReplaceItem(week, request);
    }

    @DeleteMapping("/{week}/items/{itemId}")
    public MealPlanResponse removeItem(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate week,
            @PathVariable Long itemId
    ) {
        return mealPlanService.removeItem(week, itemId);
    }
}
