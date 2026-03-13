import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { Auth } from '../../services/auth';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css'  // ✅ ADD
})
export class Navbar {

  private auth = inject(Auth);
  private router = inject(Router);

  userName: string = '';
  userInitial: string = '';
  userRole: string = '';

  constructor() {
    const token = localStorage.getItem('token');
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        this.userRole = payload.role || '';
        const email = payload.sub || '';
        this.userName = email.split('@')[0];
        this.userInitial = this.userName.charAt(0).toUpperCase();
      } catch {}
    }
  }

  logout() {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}