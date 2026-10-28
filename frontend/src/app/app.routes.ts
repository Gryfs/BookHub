import { Routes } from '@angular/router';

import { guestGuard, roleGuard } from './core/auth/auth-guards';
import { Layout } from './core/layout/layout';

export const routes: Routes = [
  {
    path: '',
    component: Layout,
    children: [
      { path: '', redirectTo: 'books', pathMatch: 'full' },
      {
        path: 'books',
        title: 'Catalogue · BookHub',
        loadComponent: () => import('./features/catalogue/catalogue').then((m) => m.Catalogue),
      },
      {
        path: 'login',
        title: 'Connexion · BookHub',
        canActivate: [guestGuard],
        loadComponent: () => import('./features/auth/login/login').then((m) => m.Login),
      },
      {
        path: 'register',
        title: 'Inscription · BookHub',
        canActivate: [guestGuard],
        loadComponent: () => import('./features/auth/register/register').then((m) => m.Register),
      },
      {
        path: 'dashboard',
        title: 'Tableau de bord · BookHub',
        canActivate: [roleGuard('USER')],
        data: { heading: 'Votre espace lecteur' },
        loadComponent: () => import('./features/dashboard/dashboard').then((m) => m.Dashboard),
      },
      {
        path: 'librarian',
        title: 'Espace bibliothécaire · BookHub',
        canActivate: [roleGuard('LIBRARIAN')],
        data: { heading: 'Espace bibliothécaire' },
        loadComponent: () => import('./features/dashboard/dashboard').then((m) => m.Dashboard),
      },
      {
        path: 'admin',
        title: 'Administration · BookHub',
        canActivate: [roleGuard('ADMIN')],
        data: { heading: 'Administration' },
        loadComponent: () => import('./features/dashboard/dashboard').then((m) => m.Dashboard),
      },
      {
        path: 'access-denied',
        title: 'Accès refusé · BookHub',
        loadComponent: () =>
          import('./features/access-denied/access-denied').then((m) => m.AccessDenied),
      },
      {
        path: '**',
        title: 'Page introuvable · BookHub',
        loadComponent: () => import('./features/not-found/not-found').then((m) => m.NotFound),
      },
    ],
  },
];
