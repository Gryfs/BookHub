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
  pseudonym: null,
  role: 'USER',
};

describe('Guards', () => {
  let session: Session;
  let router: Router;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideRouter(routes)] });
    session = TestBed.inject(Session);
    router = TestBed.inject(Router);
  });

  it('renvoie un visiteur vers la connexion', async () => {
    await RouterTestingHarness.create('/dashboard');

    expect(router.url).toBe('/login');
  });

  it('laisse un lecteur accéder à son tableau de bord', async () => {
    session.start('jwt-123', marie);

    await RouterTestingHarness.create('/dashboard');

    expect(router.url).toBe('/dashboard');
  });

  it("refuse l'espace bibliothécaire à un lecteur", async () => {
    session.start('jwt-123', marie);

    await RouterTestingHarness.create('/librarian');

    expect(router.url).toBe('/access-denied');
  });

  it("laisse un administrateur accéder à l'espace bibliothécaire", async () => {
    session.start('jwt-123', { ...marie, role: 'ADMIN' });

    await RouterTestingHarness.create('/librarian');

    expect(router.url).toBe('/librarian');
  });

  it("renvoie un utilisateur connecté vers son espace s'il ouvre la connexion", async () => {
    session.start('jwt-123', marie);

    await RouterTestingHarness.create('/login');

    expect(router.url).toBe('/dashboard');
  });
});
