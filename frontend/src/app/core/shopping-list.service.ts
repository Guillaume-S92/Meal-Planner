import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ShoppingListItemStateRequest, ShoppingListResponse } from './models';

@Injectable({ providedIn: 'root' })
export class ShoppingListService {
  private readonly baseUrl = `${environment.apiBaseUrl}/shopping-lists`;

  constructor(private readonly http: HttpClient) {}

  getWeek(week: string): Observable<ShoppingListResponse> {
    return this.http.get<ShoppingListResponse>(`${this.baseUrl}/${week}`);
  }

  updateItem(week: string, itemId: number, state: ShoppingListItemStateRequest): Observable<ShoppingListResponse> {
    return this.http.patch<ShoppingListResponse>(`${this.baseUrl}/${week}/items/${itemId}`, state);
  }
}
