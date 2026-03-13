import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface User {
  id: number;
  fullName: string;
  email: string;
  role: string;
  isActive: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class AdminService {

  private baseUrl = 'http://localhost:8080/api/admin';

  constructor(private http: HttpClient) {}

  getUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.baseUrl}/users`);
  }

  updateRole(userId: number, role: string) {
    return this.http.patch(`${this.baseUrl}/users/${userId}/role`, { role });
  }

  updateStatus(userId: number, isActive: boolean) {
    return this.http.patch(`${this.baseUrl}/users/${userId}/status`, { isActive });
  }
}