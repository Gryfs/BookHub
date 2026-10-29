import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';

import { User } from '../../shared/models/user';
import { Auth } from './auth';
import { Session } from './session';

const marie: User = {
  id: 3,
  email: 'marie.lectrice@example.fr',
  firstName: 'Marie',
  lastName: 'Dupont',
  role: 'USER',
};

function fakeToken(expiresInSeconds: number): string {
  const payload = btoa(JSON.stringify({ exp: Math.floor(Date.now() / 1000) + expiresInSeconds }));
  return `header.${payload}.signature`;
}

describe('Auth.logout', () => {
  let router: Router;

  function setup(): { auth: Auth; session: Session } {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient()],
    });
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
    return { auth: TestBed.inject(Auth), session: TestBed.inject(Session) };
  }

  it('supprime le jeton et redirige vers la connexion', () => {
    const { auth, session } = setup();
    session.start(fakeToken(3600), marie);

    auth.logout();

    expect(session.isLoggedIn()).toBe(false);
    expect(sessionStorage.getItem('bookhub.token')).toBeNull();
    expect(sessionStorage.getItem('bookhub.user')).toBeNull();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('reste déconnecté après un rechargement de la page', () => {
    const { auth, session } = setup();
    session.start(fakeToken(3600), marie);
    auth.logout();

    TestBed.resetTestingModule();
    const { session: sessionAfterRefresh } = setup();

    expect(sessionAfterRefresh.isLoggedIn()).toBe(false);
    expect(sessionAfterRefresh.getToken()).toBeNull();
  });
});
