import { Routes } from '@angular/router';

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
        path: '**',
        title: 'Page introuvable · BookHub',
        loadComponent: () => import('./features/not-found/not-found').then((m) => m.NotFound),
      },
    ],
  },
];
