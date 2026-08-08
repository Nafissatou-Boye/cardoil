import { Routes } from '@angular/router';

export const ENTREPRISE_ROUTES: Routes = [
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
        path: 'departements',
        loadComponent: () =>
          import('./departements/departements').then(m => m.DepartementsComponent),
        data: { title: 'Mes Départements' }
      },
      {
        path: 'cartes',
        loadComponent: () =>
          import('./cartes/cartes').then(m => m.CartesComponent),
        data: { title: 'Mes Cartes' }
      },
      {
        path: 'utilisateurs',
        loadComponent: () =>
          import('./utilisateurs/utilisateurs').then(m => m.UtilisateursComponent),
        data: { title: 'Utilisateurs' }
      },
      {
        path: 'utilisateurs/:id',
        loadComponent: () =>
          import('./utilisateur-detail/utilisateur-detail').then(m => m.UtilisateurDetailComponent),
        data: { title: 'Détail Utilisateur' }
      },
      {

        path: 'transactions',
        loadComponent: () =>
          import('./transactions/transactions.component').then(m => m.TransactionsComponent),
        data: { title: 'Transactions' }
      },
      {
        path: 'rapports',
        loadComponent: () =>
          import('./rapports/rapports').then(m => m.RapportsComponent),
        data: { title: 'Rapports' }
      }
    ]
  }
];