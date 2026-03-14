import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Subtask {
  id: number;
  taskId: number;
  title: string;
  isComplete: boolean;
  assignedToId?: number;
  assignedToName?: string;
  createdById: number;
  createdByName: string;
  createdAt: string;
  completedAt?: string;
}

export interface SubtaskSummary {
  total: number;
  completed: number;
}

@Injectable({ providedIn: 'root' })
export class SubtaskService {

  private baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  getSubtasks(taskId: number): Observable<Subtask[]> {
    return this.http.get<Subtask[]>(
      `${this.baseUrl}/tasks/${taskId}/subtasks`);
  }

  getSummary(taskId: number): Observable<SubtaskSummary> {
    return this.http.get<SubtaskSummary>(
      `${this.baseUrl}/tasks/${taskId}/subtasks/summary`);
  }

  create(taskId: number, title: string, assignedToId?: number): Observable<Subtask> {
    return this.http.post<Subtask>(
      `${this.baseUrl}/tasks/${taskId}/subtasks`,
      { title, assignedToId: assignedToId ?? null });
  }

  toggle(subtaskId: number): Observable<Subtask> {
    return this.http.patch<Subtask>(
      `${this.baseUrl}/subtasks/${subtaskId}/toggle`, {});
  }

  delete(subtaskId: number): Observable<void> {
    return this.http.delete<void>(
      `${this.baseUrl}/subtasks/${subtaskId}`);
  }

  update(subtaskId: number, title: string, assignedToId?: number): Observable<Subtask> {
    return this.http.put<Subtask>(
      `${this.baseUrl}/subtasks/${subtaskId}`,
      { title, assignedToId: assignedToId ?? null });
  }
}