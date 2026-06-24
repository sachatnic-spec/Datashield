import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/home',
    pathMatch: 'full'
  },
  {
    path: 'home',
    loadComponent: () => import('./features/home/home.component').then(m => m.HomeComponent)
  },
  {
    path: 'consents',
    loadComponent: () => import('./features/consents/my-consents.component').then(m => m.MyConsentsComponent)
  },
  {
    path: 'requests',
    loadComponent: () => import('./features/requests/my-requests.component').then(m => m.MyRequestsComponent)
  },
  {
    path: 'requests/new',
    loadComponent: () => import('./features/requests/new-request.component').then(m => m.NewRequestComponent)
  },
  {
    path: 'grievances',
    loadComponent: () => import('./features/grievances/my-grievances.component').then(m => m.MyGrievancesComponent)
  },
  {
    path: 'profile',
    loadComponent: () => import('./features/profile/profile.component').then(m => m.ProfileComponent)
  },
  {
    path: '**',
    redirectTo: '/home'
  }
];
