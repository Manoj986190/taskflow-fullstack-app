import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { TaskService, Task } from '../../services/task';
import { CommentService, Comment } from '../../services/comment';

@Component({
  selector: 'app-task-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './task-detail.html',
  styleUrl: './task-detail.css',
})
export class TaskDetail implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private taskService = inject(TaskService);
  private commentService = inject(CommentService);
  private cdr = inject(ChangeDetectorRef);

  taskId!: number;
  task?: Task;
  taskError: boolean = false;

  comments: Comment[] = [];
  newComment: string = '';

  isLoadingTask = false;
  isLoadingComments = false;

  // ================= INIT =================
  ngOnInit() {
    const idParam = this.route.snapshot.paramMap.get('id');

    if (!idParam) {
      this.router.navigate(['/dashboard']);
      return;
    }

    this.taskId = Number(idParam);

    this.loadTask();
    this.loadComments();
  }

  // ================= LOAD TASK =================
  loadTask() {
    this.isLoadingTask = true;
    this.taskError = false;

    this.taskService
      .getTaskById(this.taskId)
      .pipe(
        finalize(() => {
          this.isLoadingTask = false;
          this.cdr.detectChanges();
        }),
      )
      .subscribe({
        next: (task) => {
          this.task = task;
        },
        error: (err) => {
          console.error('Error loading task', err);
          this.taskError = true;
        },
      });
  }

  // ================= LOAD COMMENTS =================
  loadComments() {
    this.isLoadingComments = true;

    this.commentService
      .getComments(this.taskId)
      .pipe(
        finalize(() => {
          this.isLoadingComments = false;
          this.cdr.detectChanges();
        }),
      )
      .subscribe({
        next: (data) => {
          this.comments = data || [];
        },
        error: (err) => {
          console.error('Error loading comments', err);
          this.comments = [];
        },
      });
  }

  // ================= ADD COMMENT =================
  addComment() {
    if (!this.newComment.trim()) return;

    this.commentService.addComment(this.taskId, this.newComment).subscribe({
      next: (comment) => {
        this.comments = [...this.comments, comment];
        this.newComment = '';
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Add comment failed', err);
      },
    });
  }

  // ================= DELETE COMMENT =================
  deleteComment(commentId: number) {
    if (!confirm('Delete this comment?')) return;

    this.commentService.deleteComment(commentId).subscribe({
      next: () => {
        this.comments = this.comments.filter((c) => c.id !== commentId);
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Delete failed', err);
      },
    });
  }

  // =================update===================
  updateStatus() {
    if (!this.task) return;

    this.taskService.updateTask(this.task.id!, this.task).subscribe({
      next: (updated) => {
        this.task = updated;
      },
      error: (err) => {
        alert('You are not allowed to update this task');
        console.error(err);
      },
    });
  }

  // ================= BACK BUTTON =================
  goBack() {
    this.router.navigate(['/dashboard']);
  }

  // ================= RELATIVE TIME =================
  getRelativeTime(dateString: string): string {
    const diff = (new Date().getTime() - new Date(dateString).getTime()) / 1000;

    if (diff < 60) return 'Just now';
    if (diff < 3600) return Math.floor(diff / 60) + ' min ago';
    if (diff < 86400) return Math.floor(diff / 3600) + ' hrs ago';
    return Math.floor(diff / 86400) + ' days ago';
  }
}