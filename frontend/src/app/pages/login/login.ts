import { Component, ChangeDetectorRef, OnInit, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Auth } from '../../services/auth';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login implements OnInit, OnDestroy {

  loginData = {
    email: '',
    password: ''
  };

  errorMessage: string = '';
  showPassword: boolean = false;

  constructor(
    private auth: Auth,
    private router: Router,
    private cdr: ChangeDetectorRef) {}

  // ✅ Prevent global theme from affecting login page
  ngOnInit() {
    document.body.classList.add('auth-page');
  }

  ngOnDestroy() {
    document.body.classList.remove('auth-page');
  }

  togglePassword() {
    this.showPassword = !this.showPassword;
  }

  onSubmit() {
    this.errorMessage = '';

    this.auth.login(this.loginData).subscribe({
      next: (response) => {
        this.auth.saveToken(response.token);

        const payload = JSON.parse(atob(response.token.split('.')[1]));
        const role = payload.role;

        if (role === 'ADMIN') {
          this.router.navigate(['/admin']);
        } else if (role === 'MANAGER') {
          this.router.navigate(['/teams']);
        } else {
          this.router.navigate(['/dashboard']);
        }
      },
      error: (err) => {
        this.errorMessage =
          err.error?.message ||
          err.error?.error ||
          err.message ||
          'Invalid email or password';
        this.cdr.detectChanges();
      }
    });
  }
}