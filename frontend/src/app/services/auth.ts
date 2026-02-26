import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';

export interface LoginResponse {
  token: string;
  type: string;
  expiresIn: number;
}

@Injectable({
  providedIn: 'root',
})
export class Auth {

  private baseUrl = 'http://localhost:8080/api/auth';

  // 🔐 Authentication state
  private loggedInSubject = new BehaviorSubject<boolean>(this.checkInitialAuthState());
  isLoggedIn$ = this.loggedInSubject.asObservable();

  constructor(private http: HttpClient) {}

  // ===============================
  // API CALLS
  // ===============================

  register(data: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/register`, data);
  }

  login(data: any): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.baseUrl}/login`, data);
  }

  // ===============================
  // TOKEN MANAGEMENT
  // ===============================

  saveToken(token: string) {
    localStorage.setItem('token', token);
    this.loggedInSubject.next(true);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  logout() {
    localStorage.removeItem('token');
    this.loggedInSubject.next(false);
  }

  // ===============================
  // USER INFO FROM TOKEN
  // ===============================

  getUserName(): string | null {
    const token = this.getToken();
    if (!token) return null;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.sub; // backend stores email in "sub"
    } catch {
      return null;
    }
  }

  // ===============================
  // AUTH VALIDATION
  // ===============================

  isLoggedIn(): boolean {
    const token = this.getToken();
    if (!token) return false;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const expiry = payload.exp * 1000;

      if (Date.now() > expiry) {
        this.logout();
        return false;
      }

      return true;

    } catch {
      this.logout();
      return false;
    }
  }

  private checkInitialAuthState(): boolean {
    return this.isLoggedIn();
  }
}