export type MealType = 'LUNCH' | 'DINNER';

export interface IngredientRequest {
  name: string;
  shoppingCategory: string;
  defaultUnit?: string | null;
}

export interface IngredientResponse {
  id: number;
  name: string;
  shoppingCategory: string;
  defaultUnit?: string | null;
}

export interface RecipeIngredientRequest {
  ingredientId?: number | null;
  ingredientName?: string | null;
  shoppingCategory?: string | null;
  defaultUnit?: string | null;
  quantity: number;
  unit: string;
  optional: boolean;
  comment?: string | null;
}

export interface RecipeIngredientResponse {
  id: number;
  ingredientId: number;
  ingredientName: string;
  shoppingCategory: string;
  quantity: number;
  unit: string;
  optional: boolean;
  comment?: string | null;
}

export interface RecipeStepRequest {
  description: string;
  durationMinutes?: number | null;
}

export interface RecipeStepResponse {
  id: number;
  stepOrder: number;
  description: string;
  durationMinutes?: number | null;
}

export interface RecipeRequest {
  name: string;
  description?: string | null;
  servings: number;
  preparationTimeMinutes: number;
  cookingTimeMinutes: number;
  imageUrl?: string | null;
  category?: string | null;
  tags: string[];
  ingredients: RecipeIngredientRequest[];
  steps: RecipeStepRequest[];
}

export interface RecipeResponse {
  id: number;
  name: string;
  description?: string | null;
  servings: number;
  preparationTimeMinutes: number;
  cookingTimeMinutes: number;
  totalTimeMinutes: number;
  imageUrl?: string | null;
  category?: string | null;
  tags: string[];
  ingredients: RecipeIngredientResponse[];
  steps: RecipeStepResponse[];
  createdAt: string;
  updatedAt: string;
}

export interface MealPlanItemRequest {
  date: string;
  mealType: MealType;
  recipeId: number;
}

export interface MealPlanItemResponse {
  id: number;
  date: string;
  mealType: MealType;
  recipeId: number;
  recipeName: string;
  recipeCategory?: string | null;
}

export interface MealPlanResponse {
  id: number;
  weekStartDate: string;
  items: MealPlanItemResponse[];
}

export interface ShoppingListItemStateRequest {
  checked: boolean;
  excluded: boolean;
}

export interface ShoppingListItemResponse {
  id: number;
  ingredientId: number;
  ingredientName: string;
  quantity: number;
  unit: string;
  shoppingCategory: string;
  checked: boolean;
  excluded: boolean;
}

export interface ShoppingListResponse {
  weekStartDate: string;
  categories: Record<string, ShoppingListItemResponse[]>;
}

export interface CurrentUserResponse {
  id: number;
  username: string;
  role: string;
}

export interface AuthResponse {
  token: string;
  user: CurrentUserResponse;
}

export interface AdminUserResponse {
  id: number;
  username: string;
  role: 'USER' | 'ADMIN';
  enabled: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface AdminUserCreateRequest {
  username: string;
  password: string;
  role: 'USER' | 'ADMIN';
  enabled: boolean;
}

export interface AdminUserUpdateRequest {
  enabled?: boolean;
  role?: 'USER' | 'ADMIN';
  password?: string;
}
