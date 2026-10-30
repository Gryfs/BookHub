import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';

import { routes } from '../../app.routes';
import { User } from '../../shared/models/user';
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

describe('Guards', () => {
  function setup(): { session: Session; router: Router } {
    TestBed.configureTestingModule({
      providers: [provideRouter(routes), provideHttpClient(), provideHttpClientTesting()],
    });
    return { session: TestBed.inject(Session), router: TestBed.inject(Router) };
  }

  it('renvoie un visiteur vers la connexion', async () => {
    const { router } = setup();

    await RouterTestingHarness.create('/dashboard');

    expect(router.url).toBe('/login');
  });

  it('laisse un lecteur accéder à son tableau de bord', async () => {
    const { session, router } = setup();
    session.start(fakeToken(3600), marie);

    await RouterTestingHarness.create('/dashboard');

    expect(router.url).toBe('/dashboard');
  });

  it("refuse l'espace bibliothécaire à un lecteur", async () => {
    const { session, router } = setup();
    session.start(fakeToken(3600), marie);

    await RouterTestingHarness.create('/librarian');

    expect(router.url).toBe('/access-denied');
  });

  it("laisse un administrateur accéder à l'espace bibliothécaire", async () => {
    const { session, router } = setup();
    session.start(fakeToken(3600), { ...marie, role: 'ADMIN' });

    await RouterTestingHarness.create('/librarian');

    expect(router.url).toBe('/librarian');
  });

  it("renvoie un utilisateur connecté vers son espace s'il ouvre la connexion", async () => {
    const { session, router } = setup();
    session.start(fakeToken(3600), marie);

    await RouterTestingHarness.create('/login');

    expect(router.url).toBe('/dashboard');
  });

  it("affiche l'écran de session expirée si le jeton expire pendant la navigation", async () => {
    const { session, router } = setup();
    session.start(fakeToken(-60), marie);

    await RouterTestingHarness.create('/dashboard');

    expect(router.url).toBe('/login?expired=1');
    expect(session.isLoggedIn()).toBe(false);
  });

  it("affiche l'écran de session expirée si le jeton a expiré avant l'ouverture de l'appli", async () => {
    sessionStorage.setItem('bookhub.token', fakeToken(-60));
    sessionStorage.setItem('bookhub.user', JSON.stringify(marie));
    const { router } = setup();

    await RouterTestingHarness.create('/dashboard');

    expect(router.url).toBe('/login?expired=1');
  });

  it('refuse la gestion du catalogue à un lecteur', async () => {
    const { session, router } = setup();
    session.start(fakeToken(3600), marie);

    await RouterTestingHarness.create('/admin/books');

    expect(router.url).toBe('/access-denied');
  });

  it('ouvre la gestion du catalogue au bibliothécaire', async () => {
    const { session, router } = setup();
    session.start(fakeToken(3600), { ...marie, role: 'LIBRARIAN' });

    await RouterTestingHarness.create('/admin/books/new');

    expect(router.url).toBe('/admin/books/new');
  });

  it('réserve la suppression d’un livre à l’administrateur', async () => {
    const { session, router } = setup();
    session.start(fakeToken(3600), { ...marie, role: 'LIBRARIAN' });

    await RouterTestingHarness.create('/admin/books/7/delete');

    expect(router.url).toBe('/access-denied');
  });
});
