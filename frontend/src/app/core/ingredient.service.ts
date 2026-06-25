import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { IngredientRequest, IngredientResponse } from './models';

@Injectable({ providedIn: 'root' })
export class IngredientService {
  private readonly baseUrl = `${environment.apiBaseUrl}/ingredients`;

  constructor(private readonly http: HttpClient) {}

  list(query?: string): Observable<IngredientResponse[]> {
    const params = query ? new HttpParams().set('query', query) : undefined;
    return this.http.get<IngredientResponse[]>(this.baseUrl, { params });
  }

  create(request: IngredientRequest): Observable<IngredientResponse> {
    return this.http.post<IngredientResponse>(this.baseUrl, request);
  }
}
