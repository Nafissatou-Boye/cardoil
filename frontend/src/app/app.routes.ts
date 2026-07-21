import { Routes } from '@angular/router';
import { LoginComponent } from './auth/login/login';
import { authGuard } from './core/guards/auth-guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },

  {
    path: 'change-password',
    loadComponent: () =>
      import('./auth/change-password/change-password').then(m => m.ChangePasswordComponent)
  },

  // Routes Super Admin
  {
    path: 'super-admin',
    canActivate: [authGuard, roleGuard(['SUPER_ADMIN'])],
    loadChildren: () =>
      import('./super-admin/super-admin.routes').then(m => m.SUPER_ADMIN_ROUTES)
  },

  // Routes Admin Compagnie
  {
    path: 'admin',
    canActivate: [authGuard, roleGuard(['ADMIN_COMPAGNIE'])],
    loadChildren: () =>
      import('./admin/admin.routes').then(m => m.ADMIN_ROUTES)
  },

  // Routes Gérant
  {
    path: 'gerant',
    canActivate: [authGuard, roleGuard(['GERANT'])],
    loadChildren: () =>
      import('./gerant/gerant.routes').then(m => m.GERANT_ROUTES)
  },

  // Routes Admin Entreprise
  {
    path: 'entreprise',
    canActivate: [authGuard, roleGuard(['ADMIN_ENTREPRISE'])],
    loadChildren: () =>
      import('./entreprise/entreprise.routes').then(m => m.ENTREPRISE_ROUTES)
  },

  // Routes Admin Département
  {
    path: 'departement',
    canActivate: [authGuard, roleGuard(['ADMIN_DEPARTEMENT'])],
    loadChildren: () =>
      import('./departement/departement.routes').then(m => m.DEPARTEMENT_ROUTES)
  },

  // Page non autorisée
  {
    path: 'non-autorise',
    loadComponent: () =>
      import('./shared/non-autorise/non-autorise').then(m => m.NonAutorise)
  },

  { path: '**', redirectTo: 'login' }
];