import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { MealPlanItemRequest, MealPlanResponse } from './models';

@Injectable({ providedIn: 'root' })
export class MealPlanService {
  private readonly baseUrl = `${environment.apiBaseUrl}/meal-plans`;

  constructor(private readonly http: HttpClient) {}

  getWeek(week: string): Observable<MealPlanResponse> {
    return this.http.get<MealPlanResponse>(`${this.baseUrl}/${week}`);
  }

  saveItem(week: string, request: MealPlanItemRequest): Observable<MealPlanResponse> {
    return this.http.post<MealPlanResponse>(`${this.baseUrl}/${week}/items`, request);
  }

  removeItem(week: string, itemId: number): Observable<MealPlanResponse> {
    return this.http.delete<MealPlanResponse>(`${this.baseUrl}/${week}/items/${itemId}`);
  }
}
