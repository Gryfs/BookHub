import { TestBed } from '@angular/core/testing';

import { User } from '../../shared/models/user';
import { Session } from './session';

function fakeToken(expiresInSeconds: number): string {
  const payload = btoa(JSON.stringify({ exp: Math.floor(Date.now() / 1000) + expiresInSeconds }));
  return `header.${payload}.signature`;
}

const marie: User = {
  id: 3,
  email: 'marie.lectrice@example.fr',
  firstName: 'Marie',
  lastName: 'Dupont',
  role: 'USER',
};

describe('Session', () => {
  it("n'a pas d'utilisateur au départ", () => {
    const session = TestBed.inject(Session);

    expect(session.isLoggedIn()).toBe(false);
    expect(session.homeUrl()).toBe('/books');
  });

  it("garde le jeton et l'utilisateur après la connexion", () => {
    const session = TestBed.inject(Session);

    session.start('jwt-123', marie);

    expect(session.getToken()).toBe('jwt-123');
    expect(session.user()?.firstName).toBe('Marie');
    expect(session.homeUrl()).toBe('/dashboard');
  });

  it('retrouve la session au rechargement de la page (même onglet)', () => {
    sessionStorage.setItem('bookhub.token', fakeToken(3600));
    sessionStorage.setItem('bookhub.user', JSON.stringify(marie));

    const session = TestBed.inject(Session);

    expect(session.isLoggedIn()).toBe(true);
  });

  it('ignore une session dont le jeton a expiré', () => {
    sessionStorage.setItem('bookhub.token', fakeToken(-60));
    sessionStorage.setItem('bookhub.user', JSON.stringify(marie));

    const session = TestBed.inject(Session);

    expect(session.isLoggedIn()).toBe(false);
    expect(sessionStorage.getItem('bookhub.token')).toBeNull();
  });

  it('respecte la hiérarchie des rôles', () => {
    const session = TestBed.inject(Session);

    session.start('jwt-123', { ...marie, role: 'LIBRARIAN' });

    expect(session.hasRole('USER')).toBe(true);
    expect(session.hasRole('LIBRARIAN')).toBe(true);
    expect(session.hasRole('ADMIN')).toBe(false);
  });

  it('vide tout à la déconnexion', () => {
    const session = TestBed.inject(Session);
    session.start('jwt-123', marie);

    session.clear();

    expect(session.isLoggedIn()).toBe(false);
    expect(session.getToken()).toBeNull();
  });
});
