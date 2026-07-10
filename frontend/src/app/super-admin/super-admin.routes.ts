import { Routes } from '@angular/router';
import { EtablissementsFinanciersComponent } from './etablissements-financiers/etablissements-financiers';


export const SUPER_ADMIN_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./layout/layout').then(m => m.LayoutComponent),
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./dashboard/dashboard').then(m => m.DashboardComponent),
        data: { title: 'Dashboard' }
      },
      {
        path: 'pays',
        loadComponent: () =>
          import('./pays/pays').then(m => m.PaysComponent),
        data: { title: 'Pays' }
      },
      {
        path: 'compagnies',
        loadComponent: () =>
          import('./compagnies/compagnies').then(m => m.CompagniesComponent),
        data: { title: 'Compagnies' }
      },
      
{
  path: 'utilisateurs',
  loadComponent: () =>
    import('./utilisateurs/utilisateurs').then(m => m.UtilisateursComponent),
  data: { title: 'Administrateurs' }
},

// dans le tableau children de la route /super-admin, aux côtés de compagnies/pays/utilisateurs :
{
  path: 'etablissements-financiers',
  component: EtablissementsFinanciersComponent,
  data: { title: 'Établissements Financiers' }
},

{
  path: 'parametres',
  loadComponent: () =>
    import('./parametres/parametres').then(m => m.ParametresComponent),
  data: { title: 'Paramètres' }
}
    ]
  }
];