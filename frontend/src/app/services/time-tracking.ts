import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface TimeLog {
  id: number;
  taskId: number;
  loggedById: number;
  loggedByName: string;
  durationMinutes: number;
  logDate: string;
  note?: string;
  isManual: boolean;
  createdAt: string;
}

export interface TimerStatus {
  running: boolean;
  startTime?: string;
}

@Injectable({ providedIn: 'root' })
export class TimeTrackingService {

  private base = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  getTimerStatus(taskId: number): Observable<TimerStatus> {
    return this.http.get<TimerStatus>(
      `${this.base}/tasks/${taskId}/timer/status`);
  }

  startTimer(taskId: number): Observable<TimerStatus> {
    return this.http.post<TimerStatus>(
      `${this.base}/tasks/${taskId}/timer/start`, {});
  }

  stopTimer(taskId: number): Observable<TimeLog> {
    return this.http.post<TimeLog>(
      `${this.base}/tasks/${taskId}/timer/stop`, {});
  }

  logManual(taskId: number, hours: number,
            minutes: number, logDate: string,
            note: string): Observable<TimeLog> {
    return this.http.post<TimeLog>(
      `${this.base}/tasks/${taskId}/time-logs`,
      { hours, minutes, logDate, note });
  }

  getLogs(taskId: number): Observable<TimeLog[]> {
    return this.http.get<TimeLog[]>(
      `${this.base}/tasks/${taskId}/time-logs`);
  }

  getTotal(taskId: number): Observable<{ totalMinutes: number }> {
    return this.http.get<{ totalMinutes: number }>(
      `${this.base}/tasks/${taskId}/time-logs/total`);
  }

  deleteLog(logId: number): Observable<void> {
    return this.http.delete<void>(
      `${this.base}/time-logs/${logId}`);
  }
}