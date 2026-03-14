import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { Auth } from '../services/auth';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {

  const auth = inject(Auth);
  const router = inject(Router);
  const token = auth.getToken();

  // ✅ ADD debug log
  console.log('Interceptor:', req.method, req.url, 'Token:', token ? 'EXISTS' : 'NULL');

  let authReq = req;

  // ✅ Attach JWT if available
  if (token) {
    authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {

      // ✅ Handle expired / invalid token
      if (error.status === 401 && token) {

        // Prevent unnecessary multiple redirects
        if (router.url !== '/login') {
          auth.logout();
          router.navigate(['/login']);
        }
      }

      return throwError(() => error);
    })
  );
};