import { Component } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Auth } from '../../services/auth';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {

  registerData = {
    fullName: '',
    email: '',
    password: '',
    confirmPassword: ''
  };

  showPassword = false;
  showConfirmPassword = false;

  errorMessage: string = '';
  successMessage: string = '';

  constructor(private auth: Auth, private router: Router) {}

  togglePassword() {
    this.showPassword = !this.showPassword;
  }

  toggleConfirmPassword() {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  onSubmit(form: NgForm) {

    this.errorMessage = '';
    this.successMessage = '';

    // If form invalid or passwords don't match
    if (form.invalid || this.registerData.password !== this.registerData.confirmPassword) {

      Object.values(form.controls).forEach(control => {
        control.markAsTouched();
      });

      this.errorMessage = 'Please fill all fields correctly';
      return;
    }

    this.auth.register(this.registerData).subscribe({
      next: () => {
        this.successMessage = 'Registration successful! Redirecting to login...';

        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 1500);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Registration failed';
      }
    });
  }
}