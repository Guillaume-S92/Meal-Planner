import { HttpClient } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthResponse, CurrentUserResponse } from './models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly tokenKey = 'mealPlanner.authToken';
  private readonly baseUrl = `${environment.apiBaseUrl}/auth`;
  private readonly tokenState = signal<string | null>(localStorage.getItem(this.tokenKey));
  readonly currentUser = signal<CurrentUserResponse | null>(null);

  constructor(private readonly http: HttpClient) {}

  token(): string | null {
    return this.tokenState();
  }

  isAuthenticated(): boolean {
    return Boolean(this.tokenState());
  }

  login(username: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, { username, password }).pipe(
      tap((response) => this.saveSession(response))
    );
  }

  me(): Observable<CurrentUserResponse> {
    return this.http.get<CurrentUserResponse>(`${this.baseUrl}/me`).pipe(
      tap((user) => this.currentUser.set(user))
    );
  }

  logout(): void {
    this.clearSession();
  }

  clearSession(): void {
    localStorage.removeItem(this.tokenKey);
    this.tokenState.set(null);
    this.currentUser.set(null);
  }

  private saveSession(response: AuthResponse): void {
    localStorage.setItem(this.tokenKey, response.token);
    this.tokenState.set(response.token);
    this.currentUser.set(response.user);
  }
}
