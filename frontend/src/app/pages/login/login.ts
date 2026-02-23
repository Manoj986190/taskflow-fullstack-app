import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Auth } from '../../services/auth';

@Component({
  selector: 'app-login',
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

  constructor(private auth: Auth, private router: Router) {}

  onSubmit() {
    this.auth.login(this.loginData).subscribe({
      next: (response) => {

        // Save token in browser
        this.auth.saveToken(response.token);

        // Navigate to dashboard
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Login failed';
      }
    });
  }
}