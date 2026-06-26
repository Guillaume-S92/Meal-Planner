import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { addDays, addWeeks, formatDay, startOfWeek, toIsoDate } from '../../core/date-utils';
import { MealPlanItemResponse, MealType, RecipeResponse } from '../../core/models';
import { MealPlanService } from '../../core/meal-plan.service';
import { RecipeService } from '../../core/recipe.service';

interface DaySlot {
  iso: string;
  label: string;
}

@Component({
  selector: 'mp-planner',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatProgressBarModule,
    MatSelectModule
  ],
  templateUrl: './planner.component.html',
  styleUrl: './planner.component.scss'
})
export class PlannerComponent implements OnInit {
  protected readonly recipeQuery = new FormControl('', { nonNullable: true });
  protected weekStart = startOfWeek();
  protected days: DaySlot[] = [];
  protected recipes: RecipeResponse[] = [];
  protected items: MealPlanItemResponse[] = [];
  protected loading = true;
  protected savingKey = '';
  protected error = '';
  protected readonly mealTypes: { label: string; value: MealType }[] = [
    { label: 'Dejeuner', value: 'LUNCH' },
    { label: 'Diner', value: 'DINNER' }
  ];

  constructor(
    private readonly plans: MealPlanService,
    private readonly recipeService: RecipeService
  ) {}

  ngOnInit(): void {
    this.load();
  }

  protected get weekIso(): string {
    return toIsoDate(this.weekStart);
  }

  protected previousWeek(): void {
    this.weekStart = addWeeks(this.weekStart, -1);
    this.load();
  }

  protected nextWeek(): void {
    this.weekStart = addWeeks(this.weekStart, 1);
    this.load();
  }

  protected currentWeek(): void {
    this.weekStart = startOfWeek();
    this.load();
  }

  protected itemFor(day: string, mealType: MealType): MealPlanItemResponse | undefined {
    return this.items.find((item) => item.date === day && item.mealType === mealType);
  }

  protected recipeIdFor(day: string, mealType: MealType): number | null {
    return this.itemFor(day, mealType)?.recipeId ?? null;
  }

  protected recipeNameFor(day: string, mealType: MealType): string {
    return this.itemFor(day, mealType)?.recipeName ?? 'Aucun repas';
  }

  protected filteredRecipes(): RecipeResponse[] {
    const query = this.recipeQuery.value.trim().toLowerCase();
    if (!query) {
      return this.recipes;
    }
    return this.recipes.filter((recipe) => {
      const haystack = [
        recipe.name,
        recipe.category ?? '',
        ...(recipe.tags ?? [])
      ].join(' ').toLowerCase();
      return haystack.includes(query);
    });
  }

  protected save(day: string, mealType: MealType, recipeId: number | null): void {
    const existing = this.itemFor(day, mealType);
    if (recipeId === null && existing) {
      this.remove(existing);
      return;
    }
    if (recipeId === null) {
      return;
    }
    this.savingKey = `${day}-${mealType}`;
    this.plans.saveItem(this.weekIso, { date: day, mealType, recipeId }).subscribe({
      next: (plan) => {
        this.items = plan.items;
        this.savingKey = '';
      },
      error: () => {
        this.error = 'Impossible de mettre a jour le planning.';
        this.savingKey = '';
      }
    });
  }

  protected remove(item: MealPlanItemResponse): void {
    const previousItems = this.items;
    this.savingKey = `${item.date}-${item.mealType}`;
    this.items = this.items.filter((existing) => existing.id !== item.id);
    this.plans.removeItem(this.weekIso, item.id).subscribe({
      next: (plan) => {
        this.items = plan.items;
        this.savingKey = '';
        this.loadPlanOnly();
      },
      error: () => {
        this.items = previousItems;
        this.error = 'Impossible de retirer ce repas.';
        this.savingKey = '';
      }
    });
  }

  private load(): void {
    this.loading = true;
    this.error = '';
    this.days = Array.from({ length: 7 }, (_, index) => {
      const iso = toIsoDate(addDays(this.weekStart, index));
      return { iso, label: formatDay(iso) };
    });

    forkJoin({
      recipes: this.recipeService.list(),
      plan: this.plans.getWeek(this.weekIso)
    }).subscribe({
      next: ({ recipes, plan }) => {
        this.recipes = recipes;
        this.items = plan.items;
        this.loading = false;
      },
      error: () => {
        this.error = 'Impossible de charger le planning.';
        this.loading = false;
      }
    });
  }

  private loadPlanOnly(): void {
    this.plans.getWeek(this.weekIso).subscribe({
      next: (plan) => {
        this.items = plan.items;
      },
      error: () => {
        this.error = 'Planning supprime, mais le rechargement a echoue.';
      }
    });
  }
}
