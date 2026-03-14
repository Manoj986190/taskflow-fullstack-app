import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface UserProfile {
  id: number;
  fullName: string;
  email: string;
  role: string;
  avatarColour: string;
  bio: string;
}

export interface UserPreferences {
  theme: string;
  notifyAssigned: boolean;
  notifyComment: boolean;
  notifySubtask: boolean;
  notifyOverdue: boolean;
  notifyTeam: boolean;
}

export interface Session {
  jti: string;
  deviceHint: string;
  revokedAt?: string;
  current: boolean;
}

@Injectable({ providedIn: 'root' })
export class SettingsService {
  private base = 'http://localhost:8080/api/users/me';

  constructor(private http: HttpClient) {}

  getProfile(): Observable<UserProfile> {
    return this.http.get<UserProfile>(this.base);
  }

  updateProfile(data: Partial<UserProfile>): Observable<UserProfile> {
    return this.http.patch<UserProfile>(`${this.base}/profile`, data);
  }

  changePassword(
    currentPassword: string,
    newPassword: string,
    confirmPassword: string,
  ): Observable<void> {
    return this.http.patch<void>(`${this.base}/password`, {
      currentPassword,
      newPassword,
      confirmPassword,
    });
  }

  getPreferences(): Observable<UserPreferences> {
    return this.http.get<UserPreferences>(`${this.base}/preferences`);
  }

  updatePreferences(prefs: Partial<UserPreferences>): Observable<void> {
    return this.http.patch<void>(`${this.base}/preferences`, prefs);
  }

  deleteAccount(confirmEmail: string): Observable<void> {
     return this.http.post<void>(`${this.base}/delete-account`, { confirmEmail });
  }

  getSessions(): Observable<Session[]> {
    return this.http.get<Session[]>(`${this.base}/sessions`);
  }

  revokeSession(jti: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/sessions/${jti}`);
  }

  revokeAllOther(): Observable<void> {
    return this.http.delete<void>(`${this.base}/sessions`);
  }
}