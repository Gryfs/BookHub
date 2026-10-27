import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';

import { environment } from '../../../environments/environment';
import { TokenStorage } from './token-storage';

/**
 * Ajoute le JWT (Authorization: Bearer) aux appels vers l'API BookHub.
 * Les requêtes vers d'autres domaines ne reçoivent jamais le jeton.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = inject(TokenStorage).getToken();
  if (!token || !req.url.startsWith(environment.apiUrl)) {
    return next(req);
  }
  return next(req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }));
};
