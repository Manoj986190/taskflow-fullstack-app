import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { finalize } from 'rxjs/operators';
import { AdminService, User } from '../../services/admin';
import { Navbar } from '../navbar/navbar';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, Navbar],
  templateUrl: './admin.html',
})
export class Admin implements OnInit {

  private adminService = inject(AdminService);
  private cdr = inject(ChangeDetectorRef);

  users: User[] = [];
  loading = false;
  currentUserId: number = 0; // ✅ ADD

  ngOnInit() {
    this.extractCurrentUserId(); // ✅ ADD
    this.loadUsers();
  }

  // ✅ ADD — extract logged-in admin's ID from JWT
  extractCurrentUserId() {
    try {
      const token = localStorage.getItem('token');
      if (!token) return;
      const payload = JSON.parse(atob(token.split('.')[1]));
      this.currentUserId = payload.userId || 0;
    } catch { this.currentUserId = 0; }
  }

  loadUsers() {
    this.loading = true;
    this.adminService.getUsers()
      .pipe(finalize(() => {
        this.loading = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: (data) => { this.users = data; },
        error: (err) => { console.error('Failed to load users', err); }
      });
  }

  changeRole(user: User, role: string) {
    // ✅ Prevent admin from changing own role
    if (user.id === this.currentUserId) {
      alert('You cannot change your own role!');
      this.loadUsers(); // reload to reset dropdown
      return;
    }

    this.adminService.updateRole(user.id, role).subscribe({
      next: () => {
        user.role = role;
        this.cdr.detectChanges();
      },
      error: (err) => { console.error('Role update failed', err); }
    });
  }

  toggleStatus(user: User) {
    // ✅ Prevent admin from deactivating themselves
    if (user.id === this.currentUserId) {
      alert('You cannot deactivate your own account!');
      return;
    }

    const newStatus = !user.isActive;
    this.adminService.updateStatus(user.id, newStatus).subscribe({
      next: () => {
        user.isActive = newStatus;
        this.cdr.detectChanges();
      },
      error: (err) => { console.error('Status update failed', err); }
    });
  }

  getCountByRole(role: string): number {
    return this.users.filter(u => u.role === role).length;
  }

  getInactiveCount(): number {
    return this.users.filter(u => !u.isActive).length;
  }
}