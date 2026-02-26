import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Auth } from '../../services/auth';
import { TaskService, Task } from '../../services/task';


declare var bootstrap: any;

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit {

  private auth = inject(Auth);
  private router = inject(Router);
  private taskService = inject(TaskService);
  private cdr = inject(ChangeDetectorRef);

  userEmail: string | null = '';
  userName: string = '';
  userInitial: string = '';

  tasks: Task[] = [];
  filteredTasks: Task[] = [];

  totalTasks = 0;
  todoCount = 0;
  inProgressCount = 0;
  doneCount = 0;

  // SEARCH & FILTER
  searchQuery: string = '';
  selectedStatus: string = '';

  // CREATE MODEL
  newTask: Task = {
    title: '',
    description: '',
    dueDate: '',
    status: 'TODO'
  };

  // EDIT MODEL
  editingTask: Task | null = null;

  // ================= INIT =================
  ngOnInit() {

    if (!this.auth.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }

    this.userEmail = this.auth.getUserName();

    if (this.userEmail) {
      this.userName = this.userEmail.split('@')[0];
      this.userInitial = this.userName.charAt(0).toUpperCase();
    }

    this.loadTasks();
  }

  // ================= LOAD TASKS =================
  loadTasks() {
    this.taskService.getTasks().subscribe({
      next: (data) => {
        this.tasks = data ? [...data] : [];
        this.calculateStats();
        this.applyFilters();
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error loading tasks:', err);
        this.tasks = [];
        this.calculateStats();
        this.applyFilters();
      }
    });
  }

  // ================= CALCULATE STATS =================
  calculateStats() {
    this.totalTasks = this.tasks.length;
    this.todoCount = this.tasks.filter(t => t.status === 'TODO').length;
    this.inProgressCount = this.tasks.filter(t => t.status === 'IN_PROGRESS').length;
    this.doneCount = this.tasks.filter(t => t.status === 'DONE').length;
  }

  // ================= SEARCH & FILTER =================
  applyFilters() {
    const query = this.searchQuery.toLowerCase().trim();

    this.filteredTasks = this.tasks.filter(task => {
      const matchesSearch =
        !query ||
        task.title.toLowerCase().includes(query) ||
        task.description.toLowerCase().includes(query);

      const matchesStatus =
        !this.selectedStatus ||
        task.status === this.selectedStatus;

      return matchesSearch && matchesStatus;
    });
  }

  onSearchChange() {
    this.applyFilters();
  }

  onStatusChange() {
    this.applyFilters();
  }

  // ================= OPEN CREATE MODAL =================
  openCreateModal() {
    const modal = new bootstrap.Modal(
      document.getElementById('createTaskModal')
    );
    modal.show();
  }

  // ================= CREATE TASK =================
  createTask(form: any) {

    if (form.invalid) {
      form.control.markAllAsTouched();
      return;
    }

    this.taskService.createTask(this.newTask).subscribe({
      next: () => {

        this.newTask = {
          title: '',
          description: '',
          dueDate: '',
          status: 'TODO'
        };

        this.closeModal('createTaskModal');
        this.loadTasks(); // re-fetch from DB and auto-refresh UI

      },
      error: (err) => {
        console.error('Task creation failed:', err);
      }
    });
  }

  // ================= OPEN EDIT MODAL =================
  openEditModal(task: Task) {
    this.editingTask = { ...task };

    const modal = new bootstrap.Modal(
      document.getElementById('editTaskModal')
    );
    modal.show();
  }

  // ================= UPDATE TASK =================
  updateTask(form: any) {

    if (!this.editingTask || !this.editingTask.id) return;

    if (form.invalid) {
      form.control.markAllAsTouched();
      return;
    }

    this.taskService.updateTask(this.editingTask.id, this.editingTask)
      .subscribe({
        next: () => {

          this.closeModal('editTaskModal');
          this.editingTask = null;
          this.loadTasks(); // re-fetch from DB and auto-refresh UI

        },
        error: (err) => {
          console.error('Update failed:', err);
        }
      });
  }

  // ================= DELETE TASK =================
  deleteTask(id: number) {

    if (!confirm('Are you sure you want to delete this task?')) {
      return;
    }

    this.taskService.deleteTask(id).subscribe({
      next: () => {
        this.loadTasks(); // re-fetch from DB and auto-refresh UI
      },
      error: (err) => {
        console.error('Delete failed:', err);
      }
    });
  }

  // ================= CLOSE MODAL =================
  closeModal(id: string) {

    const modalElement = document.getElementById(id);
    if (!modalElement) return;

    const modalInstance = bootstrap.Modal.getInstance(modalElement);
    if (modalInstance) modalInstance.hide();

    setTimeout(() => {
      document.body.classList.remove('modal-open');
      const backdrops = document.getElementsByClassName('modal-backdrop');
      while (backdrops.length > 0) {
        backdrops[0].remove();
      }
    }, 200);
  }

  // ================= LOGOUT =================
  logout() {
    this.auth.logout();
    this.router.navigate(['/login']);
  }

  // ================= STATUS BADGE =================
  getStatusClass(status: string) {
    switch (status) {
      case 'TODO':
        return 'bg-secondary';
      case 'IN_PROGRESS':
        return 'bg-warning text-dark';
      case 'DONE':
        return 'bg-success';
      default:
        return 'bg-light';
    }
  }
}