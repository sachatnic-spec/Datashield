import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/dashboard',
    pathMatch: 'full'
  },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login.component').then(m => m.LoginComponent)
  },
  {
    path: '',
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'consents',
        loadComponent: () => import('./features/consents/consents.component').then(m => m.ConsentsComponent)
      },
      {
        path: 'grievances',
        loadComponent: () => import('./features/grievances/grievances.component').then(m => m.GrievancesComponent)
      },
      {
        path: 'breaches',
        loadComponent: () => import('./features/breaches/breaches.component').then(m => m.BreachesComponent)
      },
      {
        path: 'vendors',
        loadComponent: () => import('./features/vendors/vendors.component').then(m => m.VendorsComponent)
      },
      {
        path: 'reports',
        loadComponent: () => import('./features/reports/reports.component').then(m => m.ReportsComponent)
      },
      {
        path: 'settings',
        loadComponent: () => import('./features/settings/settings.component').then(m => m.SettingsComponent)
      }
    ]
  },
  {
    path: '**',
    redirectTo: '/dashboard'
  }
];
