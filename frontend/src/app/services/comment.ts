import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Comment {
  id: number;
  authorFullName: string;
  body: string;
  createdAt: string;
  isOwner: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class CommentService {

  private baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  getComments(taskId: number): Observable<Comment[]> {
    return this.http.get<Comment[]>(
      `${this.baseUrl}/tasks/${taskId}/comments`
    );
  }

  addComment(taskId: number, body: string): Observable<Comment> {
    return this.http.post<Comment>(
      `${this.baseUrl}/tasks/${taskId}/comments`,
      { body }
    );
  }

  deleteComment(commentId: number): Observable<void> {
    return this.http.delete<void>(
      `${this.baseUrl}/comments/${commentId}`
    );
  }
}