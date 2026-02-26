import { Component } from '@angular/core';
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

  constructor(private auth: Auth, private router: Router) {}

  togglePassword() {
    this.showPassword = !this.showPassword;
  }

  onSubmit() {
    this.errorMessage = '';

    this.auth.login(this.loginData).subscribe({
      next: (response) => {

        // Save token
        this.auth.saveToken(response.token);

        // Navigate to dashboard
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.errorMessage =
          err.error?.message || 'Invalid email or password';
      }
    });
  }
}