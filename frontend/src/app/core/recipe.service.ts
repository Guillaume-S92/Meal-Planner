import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { RecipeRequest, RecipeResponse } from './models';

export interface RecipeSearch {
  query?: string;
  category?: string;
  tag?: string;
}

@Injectable({ providedIn: 'root' })
export class RecipeService {
  private readonly baseUrl = `${environment.apiBaseUrl}/recipes`;

  constructor(private readonly http: HttpClient) {}

  list(search: RecipeSearch = {}): Observable<RecipeResponse[]> {
    let params = new HttpParams();
    Object.entries(search).forEach(([key, value]) => {
      if (value) {
        params = params.set(key, value);
      }
    });
    return this.http.get<RecipeResponse[]>(this.baseUrl, { params });
  }

  get(id: number): Observable<RecipeResponse> {
    return this.http.get<RecipeResponse>(`${this.baseUrl}/${id}`);
  }

  create(request: RecipeRequest): Observable<RecipeResponse> {
    return this.http.post<RecipeResponse>(this.baseUrl, request);
  }

  update(id: number, request: RecipeRequest): Observable<RecipeResponse> {
    return this.http.put<RecipeResponse>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
