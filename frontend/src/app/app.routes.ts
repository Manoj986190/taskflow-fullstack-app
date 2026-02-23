import { Routes } from '@angular/router';
import { authGuard } from './guards/auth-guard';



export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./pages/login/login').then(m => m.Login)
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./pages/register/register').then(m => m.Register)
  },
  {
    path: 'dashboard',
    canActivate: [authGuard], //authGuard added it allows accessed ones
    loadComponent: () =>
      import('./pages/dashboard/dashboard').then(m => m.Dashboard)
  },
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  }
];
