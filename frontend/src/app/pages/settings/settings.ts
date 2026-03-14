import { Component, OnInit, inject,ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { SettingsService, UserProfile, UserPreferences, Session }
  from '../../services/settings';
import { ThemeService } from '../../services/theme';
import { Auth } from '../../services/auth';
import { Navbar } from '../navbar/navbar';
import { finalize } from 'rxjs/operators';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, FormsModule, Navbar, RouterModule],
  templateUrl: './settings.html',
  styleUrl: './settings.css',
})
export class Settings implements OnInit {
  private settingsService = inject(SettingsService);
  private themeService = inject(ThemeService);
  private auth = inject(Auth);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private cdr = inject(ChangeDetectorRef);

  activeTab: string = 'profile';

  // PROFILE
  profile: UserProfile = {
    id: 0,
    fullName: '',
    email: '',
    role: '',
    avatarColour: '#2563EB',
    bio: '',
  };
  profileSaving = false;
  profileSuccess = false;
  profileError = '';

  // PASSWORD
  currentPassword = '';
  newPassword = '';
  confirmPassword = '';
  passwordSaving = false;
  passwordSuccess = false;
  passwordError = '';
  passwordStrength = 0;

  // THEME
  selectedTheme = 'LIGHT';

  // NOTIFICATIONS
  prefs: UserPreferences = {
    theme: 'LIGHT',
    notifyAssigned: true,
    notifyComment: true,
    notifySubtask: true,
    notifyOverdue: true,
    notifyTeam: true,
  };
  prefsSaving = false;

  // SESSIONS
  sessions: Session[] = [];

  // DELETE ACCOUNT
  deleteConfirmEmail = '';
  deletingAccount = false;

  currentUserRole = '';

  // ================= INIT =================
  ngOnInit() {
    this.route.queryParams.subscribe((params) => {
      this.activeTab = params['tab'] || 'profile';
    });
    this.extractRole();
    this.loadProfile();
    this.loadPreferences();
    this.loadSessions();
  }

  extractRole() {
    try {
      const token = this.auth.getToken();
      if (!token) return;
      const payload = JSON.parse(atob(token.split('.')[1]));
      this.currentUserRole = payload.role || '';
    } catch {
      this.currentUserRole = '';
    }
  }

  setTab(tab: string) {
    this.activeTab = tab;

    // ✅ Reset password form when leaving security tab
    this.currentPassword = '';
    this.newPassword = '';
    this.confirmPassword = '';
    this.passwordStrength = 0;
    this.passwordSuccess = false;
    this.passwordError = '';

    // ✅ Reset profile messages when leaving profile tab
    this.profileSuccess = false;
    this.profileError = '';

    this.router.navigate([], {
      queryParams: { tab },
      queryParamsHandling: 'merge',
    });
  }

  // ================= PROFILE =================
  loadProfile() {
    this.settingsService.getProfile().subscribe({
      next: (p) => {
        this.profile = p;
      },
      error: () => {},
    });
  }

  saveProfile() {
    this.profileSaving = true;
    this.profileSuccess = false;
    this.profileError = '';

    this.settingsService
      .updateProfile({
        fullName: this.profile.fullName,
        avatarColour: this.profile.avatarColour,
        bio: this.profile.bio,
      })
      .subscribe({
        next: (updated) => {
          console.log('Profile updated:', updated); // ✅ debug
          this.profile = updated;
          this.profileSaving = false;
          this.profileSuccess = true;
          localStorage.setItem('avatarColour', updated.avatarColour || '#2563EB');
          localStorage.setItem('userFullName', updated.fullName); // ✅ ADD
          window.dispatchEvent(new Event('storage'));
          this.cdr.detectChanges(); // ✅ force UI update
          setTimeout(() => {
            this.profileSuccess = false;
            this.cdr.detectChanges();
          }, 3000);
        },
        error: (err) => {
          console.error('Profile error:', err); // ✅ debug
          this.profileSaving = false;
          this.profileError = err.error?.message || 'Failed to save';
          this.cdr.detectChanges();
        },
        complete: () => {
          console.log('Profile save complete'); // ✅ debug
          this.profileSaving = false;
          this.cdr.detectChanges();
        },
      });
  }

  getInitials(): string {
    if (!this.profile.fullName) return '?';
    return this.profile.fullName
      .split(' ')
      .map((n) => n[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
  }

  // ================= PASSWORD =================
  checkStrength() {
    const p = this.newPassword;
    let score = 0;
    if (p.length >= 8) score++;
    if (/[A-Z]/.test(p)) score++;
    if (/[0-9]/.test(p)) score++;
    if (/[^A-Za-z0-9]/.test(p)) score++;
    this.passwordStrength = score;
  }

  getStrengthLabel(): string {
    switch (this.passwordStrength) {
      case 1:
        return 'Weak';
      case 2:
        return 'Fair';
      case 3:
        return 'Good';
      case 4:
        return 'Strong';
      default:
        return '';
    }
  }

  getStrengthClass(): string {
    switch (this.passwordStrength) {
      case 1:
        return 'bg-danger';
      case 2:
        return 'bg-warning';
      case 3:
        return 'bg-info';
      case 4:
        return 'bg-success';
      default:
        return 'bg-secondary';
    }
  }

  changePassword() {
    this.passwordSaving = true;
    this.passwordSuccess = false;
    this.passwordError = '';

    this.settingsService
      .changePassword(this.currentPassword, this.newPassword, this.confirmPassword)
      .subscribe({
        next: () => {
          this.passwordSaving = false;
          this.passwordSuccess = true;
          this.currentPassword = '';
          this.newPassword = '';
          this.confirmPassword = '';
          this.passwordStrength = 0;
          setTimeout(() => (this.passwordSuccess = false), 3000);
        },
        error: (err) => {
          this.passwordSaving = false;
          this.passwordError = err.error?.message || 'Failed to change password';
        },
      });
  }

  // ================= THEME =================
  applyTheme(theme: string) {
    this.selectedTheme = theme;
    this.themeService.setTheme(theme);
    // ✅ Also save to preferences DB
    this.settingsService.updatePreferences({ theme } as any).subscribe();
  }

  // ================= PREFERENCES =================
  loadPreferences() {
    this.settingsService.getPreferences().subscribe({
      next: (p) => {
        this.prefs = p;
        this.selectedTheme = p.theme || 'LIGHT';
        // ✅ Apply saved theme on load
        this.themeService.applyThemePublic(p.theme || 'LIGHT');
      },
      error: () => {},
    });
  }

  savePreferences() {
    this.prefsSaving = true;
    this.settingsService.updatePreferences(this.prefs).subscribe({
      next: () => {
        this.prefsSaving = false;
      },
      error: () => {
        this.prefsSaving = false;
      },
    });
  }

  // ================= SESSIONS =================
  loadSessions() {
    this.settingsService.getSessions().subscribe({
      next: (s) => {
        this.sessions = s;
      },
      error: () => {},
    });
  }

  revokeSession(jti: string) {
    if (!confirm('Revoke this session?')) return;
    this.settingsService.revokeSession(jti).subscribe({
      next: () => {
        this.sessions = this.sessions.filter((s) => s.jti !== jti);
      },
      error: (err) => {
        alert(err.error?.message || 'Failed to revoke');
      },
    });
  }

  // ================= DELETE ACCOUNT =================
  deleteAccount() {
    if (!confirm('This will permanently delete your account!')) return;
    this.deletingAccount = true;

    // ✅ DEBUG — check token exists
    const token = localStorage.getItem('token');
    console.log('Token before delete:', token ? 'EXISTS' : 'NULL');
    console.log('Token value:', token);
    console.log('Confirm email:', this.deleteConfirmEmail);

    this.settingsService.deleteAccount(this.deleteConfirmEmail).subscribe({
      next: () => {
        this.auth.logout();
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.deletingAccount = false;
        console.error('Delete error:', err);
        alert(err.error?.message || 'Failed to delete account');
      },
    });
  }

  // ================= HELPERS =================
  isAdminOrManager(): boolean {
    return this.currentUserRole === 'ADMIN' || this.currentUserRole === 'MANAGER';
  }
}