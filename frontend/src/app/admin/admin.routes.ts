import { Routes } from '@angular/router';
import { ServicesComponent } from './services/services.component';
import { TransactionsComponent } from './transactions/transactions.component';

export const ADMIN_ROUTES: Routes = [
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
  path: 'stations',
  loadComponent: () =>
    import('./stations/stations').then(m => m.StationsComponent),
  data: { title: 'Mes Stations' }
},

{
  path: 'rapports',
  loadComponent: () =>
    import('./rapports/rapports').then(m => m.RapportsComponent),
  data: { title: 'Rapports' }
},

{
  path: 'promotions',
  loadComponent: () =>
    import('./promotions/promotions').then(m => m.PromotionsComponent),
  data: { title: 'Promotions' }
},

{
  path: 'parametres',
  loadComponent: () =>
    import('./parametres/parametres').then(m => m.ParametresComponent),
  data: { title: 'Paramètres' }
},

{
  path: 'cadeaux',
  loadComponent: () =>
    import('./cadeaux/cadeaux').then(m => m.CadeauxComponent),
  data: { title: 'Catalogue de Cadeaux' }
},

{
  path: 'produits',
  loadComponent: () =>
    import('./produits/produits').then(m => m.ProduitsComponent),
  data: { title: 'Produits & Prix' }
},

{
  path: 'personnel',
  loadComponent: () =>
    import('./personnel/personnel').then(m => m.PersonnelComponent),
  data: { title: 'Mon Personnel' }
},

{ path: 'services', component: ServicesComponent, data: { title: 'Services' } },

{ path: 'transactions', component: TransactionsComponent, data: { title: 'Transactions' } },


{
  path: 'entreprises',
  loadComponent: () =>
    import('./entreprises/entreprises').then(m => m.EntreprisesComponent),
  data: { title: 'Entreprises Clientes' }
}
    ]
  }
];