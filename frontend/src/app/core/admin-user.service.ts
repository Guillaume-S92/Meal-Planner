import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AdminUserCreateRequest, AdminUserResponse, AdminUserUpdateRequest } from './models';

@Injectable({ providedIn: 'root' })
export class AdminUserService {
  private readonly baseUrl = `${environment.apiBaseUrl}/admin/users`;

  constructor(private readonly http: HttpClient) {}

  list(): Observable<AdminUserResponse[]> {
    return this.http.get<AdminUserResponse[]>(this.baseUrl);
  }

  create(request: AdminUserCreateRequest): Observable<AdminUserResponse> {
    return this.http.post<AdminUserResponse>(this.baseUrl, request);
  }

  update(id: number, request: AdminUserUpdateRequest): Observable<AdminUserResponse> {
    return this.http.patch<AdminUserResponse>(`${this.baseUrl}/${id}`, request);
  }
}
