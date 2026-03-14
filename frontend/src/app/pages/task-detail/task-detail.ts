import { Component, OnInit, OnDestroy, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { TaskService, Task } from '../../services/task';
import { CommentService, Comment } from '../../services/comment';
import { AttachmentService, Attachment } from '../../services/attachment';
import { SubtaskService, Subtask } from '../../services/subtask';
import { TimeTrackingService, TimeLog } from '../../services/time-tracking';
import { HasRoleDirective } from '../../directives/has-role';
import { Auth } from '../../services/auth';

@Component({
  selector: 'app-task-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, HasRoleDirective],
  templateUrl: './task-detail.html',
  styleUrl: './task-detail.css',
})
export class TaskDetail implements OnInit, OnDestroy {

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private taskService = inject(TaskService);
  private commentService = inject(CommentService);
  private attachmentService = inject(AttachmentService);
  private subtaskService = inject(SubtaskService);
  private timeTrackingService = inject(TimeTrackingService);
  private auth = inject(Auth);
  private cdr = inject(ChangeDetectorRef);

  taskId!: number;
  task?: Task;
  taskError: boolean = false;

  comments: Comment[] = [];
  newComment: string = '';

  isLoadingTask = false;
  isLoadingComments = false;

  // ================= ATTACHMENTS =================
  attachments: Attachment[] = [];
  isLoadingAttachments = false;
  uploadError: string = '';
  uploading = false;
  isDragOver = false;

  // ================= SUBTASKS =================
  subtasks: Subtask[] = [];
  isLoadingSubtasks = false;
  newSubtaskTitle: string = '';
  addingSubtask = false;
  togglingSubtaskId: number | null = null;

  // ================= TIME TRACKING =================
  timeLogs: TimeLog[] = [];
  isLoadingLogs = false;
  totalMinutes: number = 0;

  // Timer state
  timerRunning = false;
  timerStartTime: Date | null = null;
  timerDisplay: string = '00:00:00';
  private timerInterval: any = null;

  // Manual log form
  showManualForm = false;
  manualHours: number = 0;
  manualMinutes: number = 0;
  manualDate: string = '';
  manualNote: string = '';
  submittingManual = false;

  currentUserId: number = 0;
  currentUserRole: string = '';

  // ================= INIT =================
  ngOnInit() {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) {
      this.router.navigate(['/dashboard']);
      return;
    }

    this.taskId = Number(idParam);
    this.setTodayDate();
    this.extractTokenInfo();
    this.loadTask();
    this.loadComments();
    this.loadAttachments();
    this.loadSubtasks();
    this.loadTimeLogs();
    this.loadTimerStatus();
  }

  ngOnDestroy() {
    this.clearTimerInterval();
  }

  // ================= TOKEN INFO =================
  extractTokenInfo() {
    try {
      const token = this.auth.getToken();
      if (!token) return;
      const payload = JSON.parse(atob(token.split('.')[1]));
      this.currentUserId = payload.userId || 0;
      this.currentUserRole = payload.role || '';
    } catch {
      this.currentUserId = 0;
      this.currentUserRole = '';
    }
  }

  setTodayDate() {
    const d = new Date();
    this.manualDate = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  }

  // ================= LOAD TASK =================
  loadTask() {
    this.isLoadingTask = true;
    this.taskError = false;
    this.taskService.getTaskById(this.taskId)
      .pipe(finalize(() => {
        this.isLoadingTask = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: (task) => { this.task = task; },
        error: (err) => {
          console.error('Error loading task', err);
          this.taskError = true;
        },
      });
  }

  // ================= LOAD COMMENTS =================
  loadComments() {
    this.isLoadingComments = true;
    this.commentService.getComments(this.taskId)
      .pipe(finalize(() => {
        this.isLoadingComments = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: (data) => { this.comments = data || []; },
        error: (err) => {
          console.error('Error loading comments', err);
          this.comments = [];
        },
      });
  }

  // ================= ADD COMMENT =================
  addComment() {
    if (!this.newComment.trim()) return;
    this.commentService.addComment(this.taskId, this.newComment)
      .subscribe({
        next: (comment) => {
          this.comments = [...this.comments, comment];
          this.newComment = '';
          this.cdr.detectChanges();
        },
        error: (err) => { console.error('Add comment failed', err); },
      });
  }

  // ================= DELETE COMMENT =================
  deleteComment(commentId: number) {
    if (!confirm('Delete this comment?')) return;
    this.commentService.deleteComment(commentId)
      .subscribe({
        next: () => {
          this.comments = this.comments.filter(c => c.id !== commentId);
          this.cdr.detectChanges();
        },
        error: (err) => { console.error('Delete failed', err); },
      });
  }

  // ================= UPDATE STATUS =================
  updateStatus() {
    if (!this.task) return;
    this.taskService.updateTask(this.task.id!, this.task)
      .subscribe({
        next: (updated) => { this.task = updated; },
        error: (err) => {
          alert('You are not allowed to update this task');
          console.error(err);
        },
      });
  }

  // ================= LOAD ATTACHMENTS =================
  loadAttachments() {
    this.isLoadingAttachments = true;
    this.attachmentService.getAttachments(this.taskId)
      .pipe(finalize(() => {
        this.isLoadingAttachments = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: (data) => { this.attachments = data || []; },
        error: (err) => { console.error('Error loading attachments', err); },
      });
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) this.handleFile(input.files[0]);
  }

  onDragOver(event: DragEvent) {
    event.preventDefault();
    this.isDragOver = true;
  }

  onDragLeave() { this.isDragOver = false; }

  onDrop(event: DragEvent) {
    event.preventDefault();
    this.isDragOver = false;
    const file = event.dataTransfer?.files[0];
    if (file) this.handleFile(file);
  }

  handleFile(file: File) {
    this.uploadError = '';
    if (file.size > 5 * 1024 * 1024) {
      this.uploadError = 'File exceeds 5 MB limit.';
      return;
    }
    const allowed = [
      'image/jpeg', 'image/png', 'image/gif', 'application/pdf',
      'application/msword',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
      'application/vnd.ms-excel',
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      'text/plain', 'application/zip'
    ];
    if (!allowed.includes(file.type)) {
      this.uploadError = 'File type not allowed.';
      return;
    }
    this.uploadFile(file);
  }

  uploadFile(file: File) {
    this.uploading = true;
    this.uploadError = '';
    this.attachmentService.upload(this.taskId, file)
      .pipe(finalize(() => {
        this.uploading = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: (a) => { this.attachments = [...this.attachments, a]; },
        error: (err) => {
          this.uploadError = err.error?.message || 'Upload failed.';
        },
      });
  }

  downloadAttachment(attachment: Attachment) {
    this.attachmentService.download(attachment.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = attachment.originalName;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: (err) => { console.error('Download failed', err); },
    });
  }

  deleteAttachment(attachmentId: number) {
    if (!confirm('Delete this attachment?')) return;
    this.attachmentService.delete(attachmentId).subscribe({
      next: () => {
        this.attachments = this.attachments.filter(a => a.id !== attachmentId);
        this.cdr.detectChanges();
      },
      error: (err) => { console.error('Delete attachment failed', err); },
    });
  }

  // ================= LOAD SUBTASKS =================
  loadSubtasks() {
    this.isLoadingSubtasks = true;
    this.subtaskService.getSubtasks(this.taskId)
      .pipe(finalize(() => {
        this.isLoadingSubtasks = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: (data) => { this.subtasks = data || []; },
        error: (err) => { console.error('Error loading subtasks', err); },
      });
  }

  addSubtask() {
    if (!this.newSubtaskTitle.trim()) return;
    this.addingSubtask = true;
    this.subtaskService.create(this.taskId, this.newSubtaskTitle.trim())
      .pipe(finalize(() => {
        this.addingSubtask = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: (s) => {
          this.subtasks = [...this.subtasks, s];
          this.newSubtaskTitle = '';
        },
        error: (err) => { console.error('Add subtask failed', err); },
      });
  }

  onSubtaskKeydown(event: KeyboardEvent) {
    if (event.key === 'Enter') {
      event.preventDefault();
      this.addSubtask();
    }
  }

  toggleSubtask(subtask: Subtask) {
    if (this.isViewer()) return;
    this.togglingSubtaskId = subtask.id;
    this.subtaskService.toggle(subtask.id)
      .pipe(finalize(() => {
        this.togglingSubtaskId = null;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: (updated) => {
          this.subtasks = this.subtasks.map(s =>
            s.id === updated.id ? updated : s);
        },
        error: (err) => { console.error('Toggle failed', err); },
      });
  }

  deleteSubtask(subtaskId: number) {
    if (!confirm('Delete this subtask?')) return;
    this.subtaskService.delete(subtaskId).subscribe({
      next: () => {
        this.subtasks = this.subtasks.filter(s => s.id !== subtaskId);
        this.cdr.detectChanges();
      },
      error: (err) => { console.error('Delete subtask failed', err); },
    });
  }

  getCompletedCount(): number {
    return this.subtasks.filter(s => s.isComplete).length;
  }

  getProgressPercent(): number {
    if (this.subtasks.length === 0) return 0;
    return Math.round((this.getCompletedCount() / this.subtasks.length) * 100);
  }

  isAllDone(): boolean {
    return this.subtasks.length > 0 &&
           this.getCompletedCount() === this.subtasks.length;
  }

  canDeleteSubtask(subtask: Subtask): boolean {
    return subtask.createdById === this.currentUserId ||
           this.currentUserRole === 'ADMIN' ||
           this.currentUserRole === 'MANAGER';
  }

  // ================= LOAD TIMER STATUS =================
  loadTimerStatus() {
    if (this.isViewer()) return;
    this.timeTrackingService.getTimerStatus(this.taskId).subscribe({
      next: (status) => {
        this.timerRunning = status.running;
        if (status.running && status.startTime) {
          this.timerStartTime = new Date(status.startTime);
          this.startTimerInterval();
        }
        this.cdr.detectChanges();
      },
      error: (err) => { console.error('Timer status failed', err); },
    });
  }

  // ================= LOAD TIME LOGS =================
  loadTimeLogs() {
    this.isLoadingLogs = true;
    this.timeTrackingService.getLogs(this.taskId)
      .pipe(finalize(() => {
        this.isLoadingLogs = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: (data) => { this.timeLogs = data || []; },
        error: (err) => { console.error('Error loading logs', err); },
      });
    this.timeTrackingService.getTotal(this.taskId).subscribe({
      next: (data) => {
        this.totalMinutes = data.totalMinutes;
        this.cdr.detectChanges();
      },
      error: () => {},
    });
  }

  // ================= START TIMER =================
  startTimer() {
    this.timeTrackingService.startTimer(this.taskId).subscribe({
      next: (status) => {
        this.timerRunning = true;
        this.timerStartTime = new Date(status.startTime!);
        this.startTimerInterval();
        this.cdr.detectChanges();
      },
      error: (err) => {
        alert(err.error?.message || 'Could not start timer');
      },
    });
  }

  // ================= STOP TIMER =================
  stopTimer() {
    this.timeTrackingService.stopTimer(this.taskId).subscribe({
      next: () => {
        this.timerRunning = false;
        this.timerStartTime = null;
        this.timerDisplay = '00:00:00';
        this.clearTimerInterval();
        this.loadTimeLogs();
        this.cdr.detectChanges();
      },
      error: (err) => {
        alert(err.error?.message || 'Could not stop timer');
      },
    });
  }

  // ================= INTERVAL =================
  startTimerInterval() {
    this.clearTimerInterval();
    this.timerInterval = setInterval(() => {
      if (!this.timerStartTime) return;
      const elapsed = Math.floor(
        (new Date().getTime() - this.timerStartTime.getTime()) / 1000);
      const h = Math.floor(elapsed / 3600);
      const m = Math.floor((elapsed % 3600) / 60);
      const s = elapsed % 60;
      this.timerDisplay =
        `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
      this.cdr.detectChanges();
    }, 1000);
  }

  clearTimerInterval() {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
      this.timerInterval = null;
    }
  }

  // ================= MANUAL LOG =================
  submitManualLog() {
    if (this.manualHours === 0 && this.manualMinutes === 0) {
      alert('Please enter at least 1 minute.');
      return;
    }
    this.submittingManual = true;
    this.timeTrackingService.logManual(
      this.taskId,
      this.manualHours,
      this.manualMinutes,
      this.manualDate,
      this.manualNote
    ).pipe(finalize(() => {
      this.submittingManual = false;
      this.cdr.detectChanges();
    }))
    .subscribe({
      next: () => {
        this.manualHours = 0;
        this.manualMinutes = 0;
        this.manualNote = '';
        this.showManualForm = false;
        this.loadTimeLogs();
      },
      error: (err) => {
        alert(err.error?.message || 'Failed to log time');
      },
    });
  }

  // ================= DELETE LOG =================
  deleteLog(logId: number) {
    if (!confirm('Delete this time log?')) return;
    this.timeTrackingService.deleteLog(logId).subscribe({
      next: () => {
        this.timeLogs = this.timeLogs.filter(l => l.id !== logId);
        this.loadTimeLogs();
        this.cdr.detectChanges();
      },
      error: (err) => {
        alert(err.error?.message || 'Cannot delete this log');
      },
    });
  }

  // ================= FORMAT HELPERS =================
  formatDuration(minutes: number): string {
    const h = Math.floor(minutes / 60);
    const m = minutes % 60;
    if (h === 0) return `${m}m`;
    if (m === 0) return `${h}h`;
    return `${h}h ${m}m`;
  }

  canDeleteLog(log: TimeLog): boolean {
    return log.isManual && (
      log.loggedById === this.currentUserId ||
      this.currentUserRole === 'ADMIN' ||
      this.currentUserRole === 'MANAGER'
    );
  }

  // ================= SHARED HELPERS =================
  canDeleteAttachment(attachment: Attachment): boolean {
    return attachment.uploaderId === this.currentUserId ||
           this.currentUserRole === 'ADMIN' ||
           this.currentUserRole === 'MANAGER';
  }

  isViewer(): boolean { return this.currentUserRole === 'VIEWER'; }

  isAdminOrManager(): boolean {
    return this.currentUserRole === 'ADMIN' ||
           this.currentUserRole === 'MANAGER';
  }

  getFileIcon(mimeType: string): string {
    if (mimeType.startsWith('image/')) return '🖼️';
    if (mimeType === 'application/pdf') return '📄';
    if (mimeType.includes('word')) return '📝';
    if (mimeType.includes('excel') || mimeType.includes('sheet')) return '📊';
    if (mimeType === 'text/plain') return '📃';
    if (mimeType === 'application/zip') return '🗜️';
    return '📎';
  }

  formatFileSize(bytes: number): string {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  }

  goBack() { this.router.navigate(['/dashboard']); }

  getRelativeTime(dateString: string): string {
    const diff = (new Date().getTime() -
                  new Date(dateString).getTime()) / 1000;
    if (diff < 60) return 'Just now';
    if (diff < 3600) return Math.floor(diff / 60) + ' min ago';
    if (diff < 86400) return Math.floor(diff / 3600) + ' hrs ago';
    return Math.floor(diff / 86400) + ' days ago';
  }
}