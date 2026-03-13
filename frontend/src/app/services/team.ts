import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class TeamService {
  private baseUrl = 'http://localhost:8080/api/teams';

  constructor(private http: HttpClient) {}

  // ================= GET TEAMS =================
  getTeams(): Observable<any[]> {
    return this.http.get<any[]>(this.baseUrl);
  }

  // ================= CREATE TEAM =================
  createTeam(team: { name: string; description: string }): Observable<any> {
    return this.http.post(this.baseUrl, team);
  }

  // ================= GET TEAM BY ID =================
  getTeam(id: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/${id}`);
  }

  // ================= GET TEAM MEMBERS =================
  getTeamMembers(teamId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/${teamId}/members`);
  }

  // ================= GET TEAM TASKS =================
  getTeamTasks(teamId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/${teamId}/tasks`);
  }

  // ================= ADD MEMBER =================
  addMember(teamId: number, userId: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/${teamId}/members?userId=${userId}`, {});
  }

  // ================= REMOVE MEMBER =================
  removeMember(teamId: number, userId: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${teamId}/members/${userId}`);
  }

  // ================= DELETE TEAM =================
  deleteTeam(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${id}`);
  }

  getUsers(): Observable<any[]> {
    return this.http.get<any[]>('http://localhost:8080/api/users');
  }
}