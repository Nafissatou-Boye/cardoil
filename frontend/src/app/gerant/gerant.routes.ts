import { Routes } from '@angular/router';

export const GERANT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./layout/layout').then(m => m.LayoutComponent),
    children: [
      {
  path: 'dashboard',
  loadComponent: () =>
    import('./dashboard/dashboard').then(m => m.DashboardComponent),
  data: { title: 'Dashboard' }
},
      {
        path: 'transactions',
        loadComponent: () =>
          import('./transactions/transactions').then(m => m.TransactionsComponent),
        data: { title: 'Transactions' }
      },
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      }
    ]
  }
];