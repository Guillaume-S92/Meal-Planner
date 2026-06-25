import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MealPlanService } from '../../core/meal-plan.service';
import { RecipeService } from '../../core/recipe.service';
import { ShoppingListService } from '../../core/shopping-list.service';
import { formatDay, startOfWeek, toIsoDate } from '../../core/date-utils';
import { MealPlanItemResponse, RecipeResponse, ShoppingListResponse } from '../../core/models';

interface DashboardVm {
  week: string;
  recipeCount: number;
  plannedMeals: MealPlanItemResponse[];
  shoppingTotal: number;
  remainingShopping: number;
  nextRecipe?: RecipeResponse;
}

@Component({
  selector: 'mp-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, MatButtonModule, MatCardModule, MatIconModule, MatProgressBarModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  protected loading = true;
  protected error = '';
  protected vm?: DashboardVm;
  protected readonly formatDay = formatDay;

  constructor(
    private readonly recipes: RecipeService,
    private readonly mealPlans: MealPlanService,
    private readonly shoppingLists: ShoppingListService
  ) {}

  ngOnInit(): void {
    const week = toIsoDate(startOfWeek());
    forkJoin({
      recipes: this.recipes.list(),
      plan: this.mealPlans.getWeek(week),
      shopping: this.shoppingLists.getWeek(week)
    }).subscribe({
      next: ({ recipes, plan, shopping }) => {
        const shoppingItems = this.flattenShopping(shopping);
        this.vm = {
          week,
          recipeCount: recipes.length,
          plannedMeals: [...plan.items].sort((a, b) => `${a.date}-${a.mealType}`.localeCompare(`${b.date}-${b.mealType}`)),
          shoppingTotal: shoppingItems.length,
          remainingShopping: shoppingItems.filter((item) => !item.checked && !item.excluded).length,
          nextRecipe: recipes[0]
        };
        this.loading = false;
      },
      error: () => {
        this.error = 'Impossible de charger le tableau de bord.';
        this.loading = false;
      }
    });
  }

  private flattenShopping(shopping: ShoppingListResponse) {
    return Object.values(shopping.categories).flat();
  }
}
