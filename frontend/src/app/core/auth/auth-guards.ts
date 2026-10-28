import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { Role } from '../../shared/models/user';
import { Session } from './session';

export const authGuard: CanActivateFn = () => {
  const session = inject(Session);
  const router = inject(Router);

  return session.isLoggedIn() ? true : router.createUrlTree(['/login']);
};

export function roleGuard(role: Role): CanActivateFn {
  return () => {
    const session = inject(Session);
    const router = inject(Router);

    if (!session.isLoggedIn()) {
      return router.createUrlTree(['/login']);
    }
    return session.hasRole(role) ? true : router.createUrlTree(['/access-denied']);
  };
}

export const guestGuard: CanActivateFn = () => {
  const session = inject(Session);
  const router = inject(Router);

  return session.isLoggedIn() ? router.createUrlTree([session.homeUrl()]) : true;
};
