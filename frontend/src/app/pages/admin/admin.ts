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
  private cdr = inject(ChangeDetectorRef);  // ✅ ADD

  users: User[] = [];
  loading = false;

  ngOnInit() {
    this.loadUsers();
  }

  loadUsers() {
    this.loading = true;
    this.adminService.getUsers()
      .pipe(finalize(() => {
        this.loading = false;
        this.cdr.detectChanges();  // ✅ ADD
      }))
      .subscribe({
        next: (data) => {
          this.users = data;
        },
        error: (err) => {
          console.error('Failed to load users', err);
        }
      });
  }

  changeRole(user: User, role: string) {
    this.adminService.updateRole(user.id, role).subscribe({
      next: () => {
        user.role = role;
        this.cdr.detectChanges();  // ✅ ADD
      },
      error: (err) => {
        console.error('Role update failed', err);
      }
    });
  }

  toggleStatus(user: User) {
    const newStatus = !user.isActive;
    this.adminService.updateStatus(user.id, newStatus).subscribe({
      next: () => {
        user.isActive = newStatus;
        this.cdr.detectChanges();  // ✅ ADD
      },
      error: (err) => {
        console.error('Status update failed', err);
      }
    });
  }

  getCountByRole(role: string): number {
    return this.users.filter(u => u.role === role).length;
  }

  getInactiveCount(): number {
    return this.users.filter(u => !u.isActive).length;
  }
}