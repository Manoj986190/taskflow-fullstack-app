import { Routes } from '@angular/router';
import { authGuard } from './guards/auth-guard';
import { roleGuard } from './guards/role-guard';

export const routes: Routes = [
  // ================= LOGIN =================
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login').then((m) => m.Login),
  },

  // ================= REGISTER =================
  {
    path: 'register',
    loadComponent: () => import('./pages/register/register').then((m) => m.Register),
  },

  // ================= DASHBOARD (PROTECTED) =================
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/dashboard/dashboard').then((m) => m.Dashboard),
  },

  // ================= TEAM DETAIL (PROTECTED) =================
  {
    path: 'teams/:id',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/team-detail/team-detail').then((m) => m.TeamDetail),
  },

  // ================= TEAMS (ALL ROLES) =================
  {
    path: 'teams',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/teams/teams').then((m) => m.Teams),
  },

  // ================= ADMIN (ADMIN ONLY) =================
  {
    path: 'admin',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] },
    loadComponent: () => import('./pages/admin/admin').then((m) => m.Admin),
  },

  // ================= SETTINGS (ALL ROLES) ✅ ADD =================
  {
    path: 'settings',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/settings/settings').then((m) => m.Settings),
  },

  // ================= FORGOT PASSWORD =================
  {
    path: 'forgot-password',
    loadComponent: () =>
      import('./pages/forgot-password/forgot-password').then((m) => m.ForgotPassword),
  },

  // ================= TASK DETAIL (PROTECTED) =================
  {
    path: 'tasks/:id',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/task-detail/task-detail').then((m) => m.TaskDetail),
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
    loadComponent: () => import('./pages/not-found/not-found').then((m) => m.NotFound),
  },
];