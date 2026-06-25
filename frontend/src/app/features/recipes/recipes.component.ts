import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { RecipeResponse } from '../../core/models';
import { RecipeService } from '../../core/recipe.service';

@Component({
  selector: 'mp-recipes',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule
  ],
  templateUrl: './recipes.component.html',
  styleUrl: './recipes.component.scss'
})
export class RecipesComponent implements OnInit {
  protected readonly query = new FormControl('', { nonNullable: true });
  protected recipes: RecipeResponse[] = [];
  protected loading = true;
  protected error = '';

  constructor(private readonly recipeService: RecipeService) {}

  ngOnInit(): void {
    this.load();
    this.query.valueChanges.pipe(debounceTime(250), distinctUntilChanged()).subscribe(() => this.load());
  }

  protected load(): void {
    this.loading = true;
    this.recipeService.list({ query: this.query.value.trim() }).subscribe({
      next: (recipes) => {
        this.recipes = recipes;
        this.loading = false;
        this.error = '';
      },
      error: () => {
        this.error = 'Impossible de charger les recettes.';
        this.loading = false;
      }
    });
  }

  protected remove(recipe: RecipeResponse): void {
    const confirmed = window.confirm(`Supprimer "${recipe.name}" ?`);
    if (!confirmed) {
      return;
    }
    this.recipeService.delete(recipe.id).subscribe({
      next: () => this.load(),
      error: () => {
        this.error = 'Suppression impossible pour cette recette.';
      }
    });
  }
}
