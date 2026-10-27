import { Routes } from '@angular/router';

import { Layout } from './core/layout/layout';

/** Les pages des features sont chargées à la demande (loadComponent). */
export const routes: Routes = [
  {
    path: '',
    component: Layout,
    children: [
      {
        path: '',
        title: 'BookHub',
        loadComponent: () => import('./features/home/home').then((m) => m.Home),
      },
      {
        path: '**',
        title: 'Page introuvable · BookHub',
        loadComponent: () => import('./features/not-found/not-found').then((m) => m.NotFound),
      },
    ],
  },
];
