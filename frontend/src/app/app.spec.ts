import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';

import { routes } from './app.routes';

describe('Routing racine', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideRouter(routes)] });
  });

  it("affiche l'accueil dans le layout (header + footer)", async () => {
    const harness = await RouterTestingHarness.create('/');
    const page = harness.routeNativeElement?.closest('app-layout') ?? harness.fixture.nativeElement;

    expect(page.querySelector('app-header')).toBeTruthy();
    expect(page.querySelector('app-footer')).toBeTruthy();
    expect(page.querySelector('h1')?.textContent).toContain('Bienvenue sur BookHub');
  });

  it('affiche la page introuvable pour une URL inconnue', async () => {
    const harness = await RouterTestingHarness.create('/nexiste-pas');

    expect(harness.fixture.nativeElement.querySelector('h1')?.textContent).toContain(
      'Page introuvable',
    );
  });
});
