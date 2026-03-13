import { Component, ChangeDetectorRef } from '@angular/core';
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
export class Login {

  loginData = {
    email: '',
    password: ''
  };

  errorMessage: string = '';

  // 👁 Password toggle
  showPassword: boolean = false;

  constructor(private auth: Auth, private router: Router, private cdr: ChangeDetectorRef) {}

  togglePassword() {
    this.showPassword = !this.showPassword;
  }

  onSubmit() {
    this.errorMessage = '';

    this.auth.login(this.loginData).subscribe({
      next: (response) => {
        // Save token
        this.auth.saveToken(response.token);

        // ✅ Read role from JWT
        const payload = JSON.parse(atob(response.token.split('.')[1]));
        const role = payload.role;

        // ✅ Redirect based on role
        if (role === 'ADMIN') {
          this.router.navigate(['/admin']);
        } else if (role === 'MANAGER') {
          this.router.navigate(['/teams']);
        } else {
          this.router.navigate(['/dashboard']);
        }
      },
      error: (err) => {
        // ✅ Handle all possible error message locations
        this.errorMessage =
          err.error?.message ||
          err.error?.error ||
          err.message ||
          'Invalid email or password';

        this.cdr.detectChanges();  // ✅ Force UI update
      }
    });
  }
}