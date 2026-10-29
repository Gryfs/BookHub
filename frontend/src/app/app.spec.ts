import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';

import { routes } from './app.routes';

describe('Routing racine', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideRouter(routes), provideHttpClient(), provideHttpClientTesting()],
    });
  });

  it("redirige l'accueil vers le catalogue", async () => {
    const harness = await RouterTestingHarness.create('/');
    const page: HTMLElement = harness.fixture.nativeElement;

    expect(TestBed.inject(Router).url).toBe('/books');
    expect(page.querySelector('app-header')).toBeTruthy();
    expect(page.querySelector('app-footer')).toBeTruthy();
    expect(page.querySelector('h1')?.textContent).toContain('Catalogue des ouvrages');
  });

  it('affiche la page introuvable pour une URL inconnue', async () => {
    const harness = await RouterTestingHarness.create('/nexiste-pas');

    expect(harness.fixture.nativeElement.querySelector('h1')?.textContent).toContain(
      'Page introuvable',
    );
  });
});
