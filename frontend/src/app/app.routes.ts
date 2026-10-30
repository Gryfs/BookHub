import { Routes } from '@angular/router';

import { authGuard, guestGuard, roleGuard } from './core/auth/auth-guards';
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
        path: 'books/search',
        title: 'Recherche · BookHub',
        loadComponent: () => import('./features/search/search').then((m) => m.Search),
      },
      {
        path: 'books/:id',
        title: 'Fiche livre · BookHub',
        loadComponent: () =>
          import('./features/book-detail/book-detail').then((m) => m.BookDetailPage),
      },
      {
        path: 'loans',
        title: 'Mes emprunts · BookHub',
        canActivate: [authGuard],
        loadComponent: () => import('./features/my-loans/my-loans').then((m) => m.MyLoans),
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
        path: 'profile',
        title: 'Mon profil · BookHub',
        canActivate: [authGuard],
        loadComponent: () => import('./features/profile/profile').then((m) => m.Profile),
      },
      {
        path: 'profile/password',
        title: 'Changer le mot de passe · BookHub',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/profile/change-password/change-password').then(
            (m) => m.ChangePassword,
          ),
      },
      {
        path: 'profile/delete',
        title: 'Supprimer mon compte · BookHub',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/profile/delete-account/delete-account').then((m) => m.DeleteAccount),
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
