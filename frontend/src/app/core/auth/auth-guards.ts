import { inject } from '@angular/core';
import { CanActivateFn, Router, UrlTree } from '@angular/router';

import { Role } from '../../shared/models/user';
import { Session } from './session';

function loginPage(session: Session, router: Router): UrlTree {
  const queryParams = session.consumeExpired() ? { expired: 1 } : {};
  return router.createUrlTree(['/login'], { queryParams });
}

export const authGuard: CanActivateFn = () => {
  const session = inject(Session);
  const router = inject(Router);
  session.checkExpiration();

  return session.isLoggedIn() ? true : loginPage(session, router);
};

export function roleGuard(role: Role): CanActivateFn {
  return () => {
    const session = inject(Session);
    const router = inject(Router);
    session.checkExpiration();

    if (!session.isLoggedIn()) {
      return loginPage(session, router);
    }
    return session.hasRole(role) ? true : router.createUrlTree(['/access-denied']);
  };
}

export const guestGuard: CanActivateFn = () => {
  const session = inject(Session);
  const router = inject(Router);
  session.checkExpiration();

  return session.isLoggedIn() ? router.createUrlTree([session.homeUrl()]) : true;
};
