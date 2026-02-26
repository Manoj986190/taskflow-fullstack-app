import { Routes } from '@angular/router';
import { authGuard } from './guards/auth-guard';

export const routes: Routes = [

  // ================= LOGIN =================
  {
    path: 'login',
    loadComponent: () =>
      import('./pages/login/login').then((m) => m.Login),
  },

  // ================= REGISTER =================
  {
    path: 'register',
    loadComponent: () =>
      import('./pages/register/register').then((m) => m.Register),
  },

  // ================= DASHBOARD (PROTECTED) =================
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/dashboard/dashboard').then((m) => m.Dashboard),
  },

  // ================= FORGOT PASSWORD =================
  {
    path: 'forgot-password',
    loadComponent: () =>
      import('./pages/forgot-password/forgot-password').then(
        (m) => m.ForgotPassword
      ),
  },

  // ================= DEFAULT REDIRECT =================
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full',
  },

  // ================= 404 NOT FOUND =================
  {
    path: '**',
    loadComponent: () =>
      import('./pages/not-found/not-found').then((m) => m.NotFound),
  },

];