import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Auth } from '../../services/auth';
import { TaskService, Task } from '../../services/task';
import { UserService, AppUser } from '../../services/user';

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
  private userService = inject(UserService);
  private cdr = inject(ChangeDetectorRef);

  userEmail: string | null = '';
  userName: string = '';
  userInitial: string = '';

  currentUserId: number = 0;

  tasks: Task[] = [];
  filteredTasks: Task[] = [];
  users: AppUser[] = [];

  totalTasks = 0;
  todoCount = 0;
  inProgressCount = 0;
  doneCount = 0;

  searchQuery: string = '';
  selectedStatus: string = '';
  selectedAssignedFilter: string = '';

  sortByPriority: boolean = false; // 👈 ADD HERE

  today: string = (() => {
    const d = new Date();
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
  })();

  newTask: Task = {
    title: '',
    description: '',
    dueDate: '',
    status: 'TODO',
    priority: 'MEDIUM', // 👈 ADD
    assignedToUserId: null,
  };

  editingTask: Task | null = null;

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

    this.extractUserIdFromToken();
    this.loadUsers();
    this.loadTasks();
  }

  extractUserIdFromToken() {
    const token = localStorage.getItem('token');
    if (!token) return;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      this.currentUserId = payload.userId || 0;
    } catch {
      this.currentUserId = 0;
    }
  }

  isOwner(task: Task): boolean {
    return task.userId === this.currentUserId;
  }

  isAssignee(task: Task): boolean {
    return task.assignedToUserId === this.currentUserId;
  }

  loadUsers() {
    this.userService.getUsers().subscribe({
      next: (data) => (this.users = data || []),
      error: (err) => console.error('Error loading users:', err),
    });
  }

  loadTasks() {
    this.taskService.getTasks().subscribe({
      next: (data) => {
        this.tasks = data ? [...data] : [];
        this.calculateStats();
        this.applyFilters();
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error loading tasks:', err),
    });
  }

  calculateStats() {
    this.totalTasks = this.tasks.length;
    this.todoCount = this.tasks.filter((t) => t.status === 'TODO').length;
    this.inProgressCount = this.tasks.filter((t) => t.status === 'IN_PROGRESS').length;
    this.doneCount = this.tasks.filter((t) => t.status === 'DONE').length;
  }

  applyFilters() {
    const query = this.searchQuery.toLowerCase().trim();

    this.filteredTasks = this.tasks.filter((task) => {
      const matchesSearch =
        !query ||
        task.title.toLowerCase().includes(query) ||
        task.description.toLowerCase().includes(query);

      const matchesStatus = !this.selectedStatus || task.status === this.selectedStatus;

      const matchesAssigned =
        !this.selectedAssignedFilter ||
        (this.selectedAssignedFilter === 'me' && task.assignedToUserId === this.currentUserId);

      return matchesSearch && matchesStatus && matchesAssigned;
    });
    if (this.sortByPriority) {
      const priorityOrder: any = {
        HIGH: 1,
        MEDIUM: 2,
        LOW: 3,
      };

      this.filteredTasks.sort((a, b) => {
        // Keep status grouping stable
        if (a.status !== b.status) {
          return a.status.localeCompare(b.status);
        }

        return (
          (priorityOrder[a.priority || 'MEDIUM'] || 2) -
          (priorityOrder[b.priority || 'MEDIUM'] || 2)
        );
      });
    }
  }

  onSearchChange() {
    this.applyFilters();
  }
  onStatusChange() {
    this.applyFilters();
  }

  togglePrioritySort() {
    this.sortByPriority = !this.sortByPriority;
    this.applyFilters();
  }

  getPriorityClass(priority: string | undefined) {
    switch (priority) {
      case 'HIGH':
        return 'bg-danger';
      case 'MEDIUM':
        return 'bg-warning text-dark';
      case 'LOW':
        return 'bg-success';
      default:
        return 'bg-secondary';
    }
  }

  openCreateModal() {
    const element = document.getElementById('createTaskModal');
    if (!element) {
      console.error('Create modal not found');
      return;
    }

    const modal = new (window as any).bootstrap.Modal(element);
    element.addEventListener(
      'hidden.bs.modal',
      () => {
        this.resetCreateForm();
      },
      { once: true },
    );
    modal.show();
  }

  openEditModal(task: Task) {
    this.editingTask = { ...task };

    const element = document.getElementById('editTaskModal');
    if (!element) {
      console.error('Edit modal not found');
      return;
    }

    const modal = new (window as any).bootstrap.Modal(element);
    modal.show();
  }

  closeModal(id: string) {
    const modalElement = document.getElementById(id);
    if (!modalElement) return;

    const modalInstance = bootstrap.Modal.getInstance(modalElement);
    if (modalInstance) modalInstance.hide();
  }

  resetCreateForm() {
    this.newTask = {
      title: '',
      description: '',
      dueDate: '',
      status: 'TODO',
      assignedToUserId: null,
    };
  }

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
          status: 'TODO',
          assignedToUserId: null,
        };
        this.closeModal('createTaskModal');
        this.loadTasks();
      },
    });
  }

  updateTask(form: any) {
    if (!this.editingTask?.id) return;

    if (form.invalid) {
      form.control.markAllAsTouched();
      return;
    }

    this.taskService.updateTask(this.editingTask.id, this.editingTask).subscribe({
      next: () => {
        this.closeModal('editTaskModal');
        this.editingTask = null;

        // 🔥 Proper refresh
        setTimeout(() => {
          this.loadTasks();
        }, 200);
      },
      error: (err) => console.error(err),
    });
  }

  deleteTask(id: number, event: Event) {
    event.stopPropagation(); // VERY IMPORTANT

    if (!confirm('Are you sure you want to delete this task?')) return;

    this.taskService.deleteTask(id).subscribe({
      next: () => this.loadTasks(),
      error: (err) => {
        alert('Only task owner can delete this task');
        console.error(err);
      },
    });
  }

  openTask(id: number) {
    this.router.navigate(['/tasks', id]);
  }

  logout() {
    this.auth.logout();
    this.router.navigate(['/login']);
  }

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