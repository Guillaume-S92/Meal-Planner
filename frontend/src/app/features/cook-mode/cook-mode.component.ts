import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { RecipeResponse, RecipeStepResponse } from '../../core/models';
import { RecipeService } from '../../core/recipe.service';

@Component({
  selector: 'mp-cook-mode',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatProgressBarModule,
    MatSelectModule
  ],
  templateUrl: './cook-mode.component.html',
  styleUrl: './cook-mode.component.scss'
})
export class CookModeComponent implements OnInit {
  protected recipes: RecipeResponse[] = [];
  protected recipe?: RecipeResponse;
  protected stepIndex = 0;
  protected loading = true;
  protected error = '';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly recipeService: RecipeService
  ) {}

  protected get sortedSteps(): RecipeStepResponse[] {
    return [...(this.recipe?.steps ?? [])].sort((a, b) => a.stepOrder - b.stepOrder);
  }

  protected get currentStep(): RecipeStepResponse | undefined {
    return this.sortedSteps[this.stepIndex];
  }

  ngOnInit(): void {
    this.recipeService.list().subscribe({
      next: (recipes) => {
        this.recipes = recipes;
        const requestedId = Number(this.route.snapshot.paramMap.get('id')) || recipes[0]?.id;
        if (requestedId) {
          this.selectRecipe(requestedId, false);
        } else {
          this.loading = false;
        }
      },
      error: () => {
        this.error = 'Impossible de charger les recettes.';
        this.loading = false;
      }
    });
  }

  protected selectRecipe(id: number, updateUrl = true): void {
    this.loading = true;
    this.recipeService.get(id).subscribe({
      next: (recipe) => {
        this.recipe = recipe;
        this.stepIndex = 0;
        this.loading = false;
        if (updateUrl) {
          this.router.navigate(['/cook', recipe.id]);
        }
      },
      error: () => {
        this.error = 'Impossible de charger cette recette.';
        this.loading = false;
      }
    });
  }

  protected previousStep(): void {
    this.stepIndex = Math.max(0, this.stepIndex - 1);
  }

  protected nextStep(): void {
    this.stepIndex = Math.min(this.sortedSteps.length - 1, this.stepIndex + 1);
  }
}
