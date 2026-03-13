import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Attachment {
  id: number;
  taskId: number;
  uploaderId: number;
  uploaderName: string;
  originalName: string;
  mimeType: string;
  fileSizeBytes: number;
  uploadedAt: string;
}

@Injectable({ providedIn: 'root' })
export class AttachmentService {

  private baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  // ================= LIST =================
  getAttachments(taskId: number): Observable<Attachment[]> {
    return this.http.get<Attachment[]>(
      `${this.baseUrl}/tasks/${taskId}/attachments`);
  }

  // ================= UPLOAD =================
  upload(taskId: number, file: File): Observable<Attachment> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<Attachment>(
      `${this.baseUrl}/tasks/${taskId}/attachments`, formData);
  }

  // ================= DOWNLOAD =================
  download(attachmentId: number): Observable<Blob> {
    return this.http.get(
      `${this.baseUrl}/attachments/${attachmentId}/download`,
      { responseType: 'blob' });
  }

  // ================= DELETE =================
  delete(attachmentId: number): Observable<void> {
    return this.http.delete<void>(
      `${this.baseUrl}/attachments/${attachmentId}`);
  }
}