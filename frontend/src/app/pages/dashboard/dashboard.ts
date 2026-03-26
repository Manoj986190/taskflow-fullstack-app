import { Component, OnInit, AfterViewChecked, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Auth } from '../../services/auth';
import { TaskService, Task, Activity } from '../../services/task';
import { UserService, AppUser } from '../../services/user';
import { TeamService } from '../../services/team';
import { TaskDueDatePipe } from '../../pipes/task-due-date-pipe';
import { Navbar } from '../navbar/navbar';
import { SubtaskService } from '../../services/subtask';
import { HasRoleDirective } from '../../directives/has-role';

declare var bootstrap: any;
declare var Chart: any;

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, TaskDueDatePipe, Navbar, HasRoleDirective],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit, AfterViewChecked {
  private auth = inject(Auth);
  private router = inject(Router);
  private taskService = inject(TaskService);
  private userService = inject(UserService);
  private teamService = inject(TeamService); // ✅ ADD
  private cdr = inject(ChangeDetectorRef);
  private subtaskService = inject(SubtaskService);

  userEmail: string | null = '';
  currentUserId: number = 0;

  tasks: Task[] = [];
  filteredTasks: Task[] = [];
  users: AppUser[] = [];
  teams: any[] = []; // ✅ ADD

  totalTasks = 0;
  todoCount = 0;
  inProgressCount = 0;
  doneCount = 0;

  searchQuery: string = '';
  selectedStatus: string = '';
  selectedAssignedFilter: string = '';

  sortByPriority: boolean = false;

  // ANALYTICS
  summary: any = null;
  showAnalytics = false;
  statusChart: any;
  priorityChart: any;
  chartsRendered = false;

  activities: Activity[] = [];
  loadingActivity = false;

  overdueCount = 0;
  dueTodayCount = 0;
  showDueBanner = true;

  subtaskSummaries: Map<number, { total: number; completed: number }> = new Map();

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
    priority: 'MEDIUM',
    assignedToUserId: null,
    teamId: null, // ✅ ADD
  };

  editingTask: Task | null = null;

  ngOnInit() {
    if (!this.auth.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }

    this.userEmail = this.auth.getUserName();
    this.extractUserIdFromToken();
    this.loadUsers();
    this.loadTasks();
    this.loadActivity();
    this.loadTeams(); // ✅ ADD
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

  // ✅ ADD
  loadTeams() {
    this.teamService.getTeams().subscribe({
      next: (data) => (this.teams = data || []),
      error: (err) => console.error('Error loading teams:', err),
    });
  }

  loadTasks() {
    this.taskService.getTasks().subscribe({
      next: (data) => {
        this.tasks = data ? [...data] : [];
        this.calculateDueAlerts();
        this.calculateStats();
        this.applyFilters();
        this.loadSubtaskSummaries();
        this.chartsRendered = false;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error loading tasks:', err),
    });
  }

  calculateDueAlerts() {
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    this.overdueCount = 0;
    this.dueTodayCount = 0;

    this.tasks.forEach((task) => {
      if (task.status === 'DONE') return;

      const due = new Date(task.dueDate);
      due.setHours(0, 0, 0, 0);

      if (due < today) this.overdueCount++;
      if (due.getTime() === today.getTime()) this.dueTodayCount++;
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
      const priorityOrder: any = { HIGH: 1, MEDIUM: 2, LOW: 3 };

      this.filteredTasks.sort((a, b) => {
        if (a.status !== b.status) return a.status.localeCompare(b.status);
        return (
          (priorityOrder[a.priority || 'MEDIUM'] || 2) -
          (priorityOrder[b.priority || 'MEDIUM'] || 2)
        );
      });
    }
  }

  // ================= SUBTASK SUMMARIES =================
  loadSubtaskSummaries() {
    this.tasks.forEach((task) => {
      if (!task.id) return;
      this.subtaskService.getSummary(task.id).subscribe({
        next: (summary) => {
          this.subtaskSummaries.set(task.id!, summary);
          this.cdr.detectChanges();
        },
        error: () => {},
      });
    });
  }

  getSubtaskSummary(taskId: number) {
    return this.subtaskSummaries.get(taskId) || { total: 0, completed: 0 };
  }

  getProgressPercent(taskId: number): number {
    const s = this.getSubtaskSummary(taskId);
    if (s.total === 0) return 0;
    return Math.round((s.completed / s.total) * 100);
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

  // ✅ ADD — filter out VIEWERs from assign dropdown
  get assignableUsers(): AppUser[] {
    return this.users.filter((u) => u.role !== 'VIEWER');
  }

  openCreateModal() {
    const element = document.getElementById('createTaskModal');
    if (!element) return;

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
    if (!element) return;
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
      priority: 'MEDIUM',
      assignedToUserId: null,
      teamId: null, // ✅ ADD
    };
  }

  createTask(form: any) {
    if (form.invalid) {
      form.control.markAllAsTouched();
      return;
    }

    this.newTask.status = 'TODO';

    this.taskService.createTask(this.newTask).subscribe({
      next: () => {
        this.newTask = {
          title: '',
          description: '',
          dueDate: '',
          status: 'TODO',
          priority: 'MEDIUM',
          assignedToUserId: null,
          teamId: null, // ✅ ADD
        };
        this.closeModal('createTaskModal');
        this.loadTasks();
        this.loadAnalytics();
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
        setTimeout(() => {
          this.loadTasks();
          this.loadAnalytics();
        }, 200);
      },
      error: (err) => console.error(err),
    });
  }

  deleteTask(id: number, event: Event) {
    event.stopPropagation();
    if (!confirm('Are you sure you want to delete this task?')) return;

    this.taskService.deleteTask(id).subscribe({
      next: () => {
        this.loadTasks();
        this.loadAnalytics();
      },
      error: (err) => {
        alert('Only task owner can delete this task');
        //alert(err.error?.message || err.message || 'Failed to delete task');
        console.error(err);
      },
    });
  }

  openTask(id: number) {
    this.router.navigate(['/tasks', id]);
  }

  loadAnalytics() {
    this.taskService.getSummary().subscribe({
      next: (data) => {
        this.summary = data;
        this.chartsRendered = false;
        this.cdr.detectChanges();
        setTimeout(() => {
          this.renderCharts();
        }, 50);
      },
      error: (err) => console.error('Summary load error', err),
    });
  }

  toggleAnalytics() {
    this.showAnalytics = !this.showAnalytics;

    if (this.showAnalytics) {
      this.chartsRendered = false;
      if (!this.summary) {
        this.loadAnalytics();
      } else {
        setTimeout(() => {
          this.renderCharts();
        }, 50);
      }
    } else {
      if (this.statusChart) {
        this.statusChart.destroy();
        this.statusChart = null;
      }
      if (this.priorityChart) {
        this.priorityChart.destroy();
        this.priorityChart = null;
      }
    }
  }

  ngAfterViewChecked() {
    if (this.showAnalytics && this.summary && !this.chartsRendered) {
      this.renderCharts();
      this.chartsRendered = true;
    }
  }

  renderCharts() {
    const statusCanvas = document.getElementById('statusChart') as HTMLCanvasElement;
    const priorityCanvas = document.getElementById('priorityChart') as HTMLCanvasElement;
    if (!statusCanvas || !priorityCanvas) return;

    const statusCtx = statusCanvas.getContext('2d');
    const priorityCtx = priorityCanvas.getContext('2d');
    if (!statusCtx || !priorityCtx) return;

    if (this.statusChart) this.statusChart.destroy();
    if (this.priorityChart) this.priorityChart.destroy();

    this.statusChart = new Chart(statusCtx, {
      type: 'doughnut',
      data: {
        labels: ['TODO', 'IN PROGRESS', 'DONE'],
        datasets: [
          {
            data: [
              this.summary.byStatus.todo,
              this.summary.byStatus.inProgress,
              this.summary.byStatus.done,
            ],
            backgroundColor: ['#0d6efd', '#ffc107', '#198754'],
          },
        ],
      },
      options: { maintainAspectRatio: false },
    });

    this.priorityChart = new Chart(priorityCtx, {
      type: 'bar',
      data: {
        labels: ['HIGH', 'MEDIUM', 'LOW'],
        datasets: [
          {
            label: 'Tasks',
            data: [
              this.summary.byPriority.high,
              this.summary.byPriority.medium,
              this.summary.byPriority.low,
            ],
            backgroundColor: ['#dc3545', '#ffc107', '#198754'],
          },
        ],
      },
      options: { maintainAspectRatio: false },
    });
  }

  loadActivity() {
    this.loadingActivity = true;
    this.taskService.getActivity().subscribe({
      next: (data) => {
        this.activities = data;
        this.loadingActivity = false;
      },
      error: (err) => {
        console.error('Activity load error', err);
        this.loadingActivity = false;
      },
    });
  }

  getRelativeTime(dateString: string): string {
    const now = new Date().getTime();
    const past = new Date(dateString).getTime();
    const diff = Math.floor((now - past) / 1000);

    if (diff < 60) return diff + ' sec ago';
    const minutes = Math.floor(diff / 60);
    if (minutes < 60) return minutes + ' min ago';
    const hours = Math.floor(minutes / 60);
    if (hours < 24) return hours + ' hr ago';
    const days = Math.floor(hours / 24);
    return days + ' days ago';
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