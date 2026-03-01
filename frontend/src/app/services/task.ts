// import { Injectable } from '@angular/core';
// import { HttpClient } from '@angular/common/http';
// import { Observable } from 'rxjs';

// export interface Task {
//   id?: number;
//   title: string;
//   description: string;
//   dueDate: string;
//   status: string;
// }

// @Injectable({
//   providedIn: 'root'
// })
// export class TaskService {

//   private baseUrl = 'http://localhost:8080/api/tasks';

//   constructor(private http: HttpClient) {}

//   // ✅ GET ALL TASKS
//   getTasks(): Observable<Task[]> {
//     return this.http.get<Task[]>(this.baseUrl);
//   }

//   // ✅ GET SINGLE TASK (NEW METHOD — REQUIRED)
//   getTaskById(id: number): Observable<Task> {
//     return this.http.get<Task>(`${this.baseUrl}/${id}`);
//   }

//   // ✅ CREATE TASK
//   createTask(task: Task): Observable<Task> {
//     return this.http.post<Task>(this.baseUrl, task);
//   }

//   // ✅ UPDATE TASK
//   updateTask(id: number, task: Task): Observable<Task> {
//     return this.http.put<Task>(`${this.baseUrl}/${id}`, task);
//   }

//   // ✅ DELETE TASK
//   deleteTask(id: number): Observable<void> {
//     return this.http.delete<void>(`${this.baseUrl}/${id}`);
//   }
// }

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Task {
  id?: number;
  title: string;
  description: string;
  dueDate: string;
  status: string;

  // ✅ NEW FIELDS (Assignment)
  userId?: number;                // OWNER ID
  assignedToUserId?: number | null;
  assignedToFullName?: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class TaskService {

  private baseUrl = 'http://localhost:8080/api/tasks';

  constructor(private http: HttpClient) {}

  getTasks(): Observable<Task[]> {
    return this.http.get<Task[]>(this.baseUrl);
  }

  getTaskById(id: number): Observable<Task> {
    return this.http.get<Task>(`${this.baseUrl}/${id}`);
  }

  createTask(task: Task): Observable<Task> {
    return this.http.post<Task>(this.baseUrl, task);
  }

  updateTask(id: number, task: Task): Observable<Task> {
    return this.http.put<Task>(`${this.baseUrl}/${id}`, task);
  }

  deleteTask(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}