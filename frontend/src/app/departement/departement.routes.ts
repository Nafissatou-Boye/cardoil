import { Routes } from '@angular/router';

export const DEPARTEMENT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./layout/layout').then(m => m.LayoutComponent),
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./dashboard/dashboard').then(m => m.Dashboard),
        data: { title: 'Dashboard' }
      },
      {
        path: 'employes',
        loadComponent: () =>
          import('./employes/employes').then(m => m.EmployesComponent),
        data: { title: 'Mes Employés' }
      },
      {
        path: 'cartes',
        loadComponent: () =>
          import('./cartes/cartes').then(m => m.CartesComponent),
        data: { title: 'Mes Cartes' }
      }
    ]
  }
];