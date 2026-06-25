import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { RecipeRequest, RecipeResponse } from '../../core/models';
import { RecipeService } from '../../core/recipe.service';

type IngredientForm = FormGroup<{
  ingredientName: FormControl<string>;
  shoppingCategory: FormControl<string>;
  quantity: FormControl<number>;
  unit: FormControl<string>;
  optional: FormControl<boolean>;
  comment: FormControl<string>;
}>;

type StepForm = FormGroup<{
  description: FormControl<string>;
  durationMinutes: FormControl<number>;
}>;

@Component({
  selector: 'mp-recipe-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatCheckboxModule,
    MatChipsModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule
  ],
  templateUrl: './recipe-form.component.html',
  styleUrl: './recipe-form.component.scss'
})
export class RecipeFormComponent implements OnInit {
  protected readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(120)]],
    description: [''],
    servings: [4, [Validators.required, Validators.min(1)]],
    preparationTimeMinutes: [10, [Validators.required, Validators.min(0)]],
    cookingTimeMinutes: [20, [Validators.required, Validators.min(0)]],
    imageUrl: [''],
    category: [''],
    tagInput: [''],
    tags: this.fb.array<FormControl<string>>([]),
    ingredients: this.fb.array<IngredientForm>([]),
    steps: this.fb.array<StepForm>([])
  });

  protected loading = false;
  protected saving = false;
  protected error = '';
  protected recipeId?: number;

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly recipes: RecipeService
  ) {}

  get ingredients(): FormArray<IngredientForm> {
    return this.form.controls.ingredients;
  }

  get steps(): FormArray<StepForm> {
    return this.form.controls.steps;
  }

  get tags(): FormArray<FormControl<string>> {
    return this.form.controls.tags;
  }

  ngOnInit(): void {
    this.recipeId = Number(this.route.snapshot.paramMap.get('id')) || undefined;
    if (this.recipeId) {
      this.loading = true;
      this.recipes.get(this.recipeId).subscribe({
        next: (recipe) => {
          this.patchRecipe(recipe);
          this.loading = false;
        },
        error: () => {
          this.error = 'Impossible de charger cette recette.';
          this.loading = false;
        }
      });
      return;
    }
    this.addIngredient();
    this.addStep();
  }

  protected addIngredient(): void {
    this.ingredients.push(this.fb.nonNullable.group({
      ingredientName: ['', Validators.required],
      shoppingCategory: ['Epicerie', Validators.required],
      quantity: [1, [Validators.required, Validators.min(0.01)]],
      unit: ['piece', Validators.required],
      optional: [false],
      comment: ['']
    }));
  }

  protected removeIngredient(index: number): void {
    if (this.ingredients.length > 1) {
      this.ingredients.removeAt(index);
    }
  }

  protected addStep(): void {
    this.steps.push(this.fb.nonNullable.group({
      description: ['', Validators.required],
      durationMinutes: [0, [Validators.required, Validators.min(0)]]
    }));
  }

  protected removeStep(index: number): void {
    if (this.steps.length > 1) {
      this.steps.removeAt(index);
    }
  }

  protected addTag(): void {
    const value = this.form.controls.tagInput.value.trim();
    if (value && !this.tags.value.includes(value)) {
      this.tags.push(this.fb.nonNullable.control(value));
    }
    this.form.controls.tagInput.setValue('');
  }

  protected removeTag(index: number): void {
    this.tags.removeAt(index);
  }

  protected save(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.saving) {
      return;
    }
    const request = this.toRequest();
    this.saving = true;
    const action = this.recipeId
      ? this.recipes.update(this.recipeId, request)
      : this.recipes.create(request);

    action.subscribe({
      next: (recipe) => {
        this.saving = false;
        this.router.navigate(['/recipes', recipe.id, 'edit']);
      },
      error: () => {
        this.error = 'Enregistrement impossible. Verifiez les champs obligatoires.';
        this.saving = false;
      }
    });
  }

  private patchRecipe(recipe: RecipeResponse): void {
    this.form.patchValue({
      name: recipe.name,
      description: recipe.description ?? '',
      servings: recipe.servings,
      preparationTimeMinutes: recipe.preparationTimeMinutes,
      cookingTimeMinutes: recipe.cookingTimeMinutes,
      imageUrl: recipe.imageUrl ?? '',
      category: recipe.category ?? ''
    });

    this.tags.clear();
    recipe.tags.forEach((tag) => this.tags.push(this.fb.nonNullable.control(tag)));

    this.ingredients.clear();
    recipe.ingredients.forEach((ingredient) => this.ingredients.push(this.fb.nonNullable.group({
      ingredientName: [ingredient.ingredientName, Validators.required],
      shoppingCategory: [ingredient.shoppingCategory, Validators.required],
      quantity: [Number(ingredient.quantity), [Validators.required, Validators.min(0.01)]],
      unit: [ingredient.unit, Validators.required],
      optional: [ingredient.optional],
      comment: [ingredient.comment ?? '']
    })));

    this.steps.clear();
    [...recipe.steps]
      .sort((a, b) => a.stepOrder - b.stepOrder)
      .forEach((step) => this.steps.push(this.fb.nonNullable.group({
        description: [step.description, Validators.required],
        durationMinutes: [step.durationMinutes ?? 0, [Validators.required, Validators.min(0)]]
      })));
  }

  private toRequest(): RecipeRequest {
    const value = this.form.getRawValue();
    return {
      name: value.name.trim(),
      description: value.description.trim() || null,
      servings: value.servings,
      preparationTimeMinutes: value.preparationTimeMinutes,
      cookingTimeMinutes: value.cookingTimeMinutes,
      imageUrl: value.imageUrl.trim() || null,
      category: value.category.trim() || null,
      tags: value.tags,
      ingredients: value.ingredients.map((ingredient) => ({
        ingredientName: ingredient.ingredientName.trim(),
        shoppingCategory: ingredient.shoppingCategory.trim(),
        defaultUnit: ingredient.unit.trim(),
        quantity: ingredient.quantity,
        unit: ingredient.unit.trim(),
        optional: ingredient.optional,
        comment: ingredient.comment.trim() || null
      })),
      steps: value.steps.map((step) => ({
        description: step.description.trim(),
        durationMinutes: step.durationMinutes
      }))
    };
  }
}
