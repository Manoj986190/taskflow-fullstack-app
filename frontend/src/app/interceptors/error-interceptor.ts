import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';
import { ToastService } from '../services/toast';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {

  const toastService = inject(ToastService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {

      // ❌ Do NOT interfere with login/register requests
      if (req.url.includes('/api/auth/login') ||
          req.url.includes('/api/auth/register')) {
        return throwError(() => error);
      }

      // ❌ Let auth interceptor handle 401 (logout + redirect)
      if (error.status === 401) {
        return throwError(() => error);
      }

      let message = 'Something went wrong. Please try again.';

      if (error.status === 0) {
        message = 'Network error. Please check your connection.';
      }

      else if (error.status === 400) {
        message = error.error?.message || 'Invalid request.';
      }

      else if (error.status === 403) {
        message = 'You are not allowed to perform this action.';
      }

      else if (error.status === 404) {
        message = 'Requested resource not found.';
      }

      else if (error.status === 500) {
        message = 'Server error. Please try again later.';
      }

      toastService.show(message, 'error');

      return throwError(() => error);
    })
  );
};