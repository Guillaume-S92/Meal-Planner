import { Routes } from '@angular/router';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { RecipesComponent } from './features/recipes/recipes.component';
import { RecipeFormComponent } from './features/recipe-form/recipe-form.component';
import { PlannerComponent } from './features/planner/planner.component';
import { ShoppingListComponent } from './features/shopping-list/shopping-list.component';
import { CookModeComponent } from './features/cook-mode/cook-mode.component';
import { LoginComponent } from './features/login/login.component';
import { AdminUsersComponent } from './features/admin-users/admin-users.component';
import { authGuard } from './core/auth.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent, title: 'Connexion' },
  { path: 'dashboard', component: DashboardComponent, title: 'Tableau de bord', canActivate: [authGuard] },
  { path: 'recipes', component: RecipesComponent, title: 'Recettes', canActivate: [authGuard] },
  { path: 'recipes/new', component: RecipeFormComponent, title: 'Nouvelle recette', canActivate: [authGuard] },
  { path: 'recipes/:id/edit', component: RecipeFormComponent, title: 'Modifier une recette', canActivate: [authGuard] },
  { path: 'planner', component: PlannerComponent, title: 'Planning', canActivate: [authGuard] },
  { path: 'shopping', component: ShoppingListComponent, title: 'Courses', canActivate: [authGuard] },
  { path: 'cook', component: CookModeComponent, title: 'Mode cuisine', canActivate: [authGuard] },
  { path: 'cook/:id', component: CookModeComponent, title: 'Mode cuisine', canActivate: [authGuard] },
  { path: 'admin/users', component: AdminUsersComponent, title: 'Utilisateurs', canActivate: [authGuard] },
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  { path: '**', redirectTo: 'dashboard' }
];
