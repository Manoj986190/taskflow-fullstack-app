import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
import { Auth } from '../../services/auth';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css'
})
export class Navbar implements OnInit, OnDestroy {

  private auth = inject(Auth);
  private router = inject(Router);

  userName: string = '';
  userInitial: string = '';
  userRole: string = '';
  avatarColour: string = '#2563EB';

  private storageListener = () => this.loadFromToken();

  ngOnInit() {
    this.loadFromToken();

    // ✅ Reload on every route change
    this.router.events
      .pipe(filter(e => e instanceof NavigationEnd))
      .subscribe(() => this.loadFromToken());

    // ✅ Reload when settings page dispatches storage event
    window.addEventListener('storage', this.storageListener);
  }

  ngOnDestroy() {
    window.removeEventListener('storage', this.storageListener);
  }

  loadFromToken() {
    const token = localStorage.getItem('token');
    if (!token) return;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      this.userRole = payload.role || '';

      // ✅ Read from localStorage first — updated by settings page
      const storedName = localStorage.getItem('userFullName');
      const jwtName = payload.fullName || payload.sub?.split('@')[0] || '';
      this.userName = storedName || jwtName;
      this.userInitial = this.userName.charAt(0).toUpperCase();

      // ✅ Avatar colour from localStorage
      const storedColour = localStorage.getItem('avatarColour');
      if (storedColour) this.avatarColour = storedColour;
    } catch {}
  }

  logout() {
    localStorage.removeItem('userFullName');  // ✅ ADD
    this.auth.logout();
    this.router.navigate(['/login']);
  }

  goToSettings() {
    this.router.navigate(['/settings']);
  }
}