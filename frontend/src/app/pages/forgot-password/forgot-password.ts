import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './forgot-password.html',
})
export class ForgotPassword {

  email: string = '';
  message: string = '';

  onSubmit() {
    // UI only — no backend
    this.message =
      'If this email exists, a reset link has been sent.';
  }
}