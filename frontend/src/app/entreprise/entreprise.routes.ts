import { Routes } from '@angular/router';

export const ENTREPRISE_ROUTES: Routes = [
  {
    path: 'dashboard',
    loadComponent: () =>
      import('./dashboard/dashboard').then(m => m.Dashboard)
  }
];