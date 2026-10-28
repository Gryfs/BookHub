import { provideHttpClient } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { Session } from '../../auth/session';
import { Header } from './header';

describe('Header', () => {
  let fixture: ComponentFixture<Header>;
  let element: HTMLElement;
  let session: Session;

  beforeEach(async () => {
    TestBed.configureTestingModule({
      imports: [Header],
      providers: [provideRouter([{ path: '**', children: [] }]), provideHttpClient()],
    });
    session = TestBed.inject(Session);
    fixture = TestBed.createComponent(Header);
    element = fixture.nativeElement;
    await fixture.whenStable();
  });

  function navLinks(): (string | undefined)[] {
    return Array.from(element.querySelectorAll('nav a')).map((a) => a.textContent?.trim());
  }

  it('affiche les liens du visiteur', () => {
    expect(navLinks()).toEqual(['Catalogue', 'Recherche', 'Connexion']);
    expect(element.querySelector('.account')?.textContent).toContain('Visiteur');
  });

  it("affiche le nom et le rôle de l'utilisateur connecté", async () => {
    session.start('jwt-123', {
      id: 2,
      email: 'ahmed.biblio@quartier-solidaire.fr',
      firstName: 'Ahmed',
      lastName: 'Benali',
      pseudonym: null,
      role: 'LIBRARIAN',
    });
    await fixture.whenStable();

    expect(navLinks()).toEqual(['Pilotage', 'Catalogue', 'Recherche']);
    expect(element.querySelector('.account')?.textContent).toContain('Ahmed B. · LIBRARIAN');
  });

  it('déconnecte l’utilisateur', async () => {
    session.start('jwt-123', {
      id: 3,
      email: 'marie.lectrice@example.fr',
      firstName: 'Marie',
      lastName: 'Dupont',
      pseudonym: null,
      role: 'USER',
    });
    await fixture.whenStable();

    element.querySelector<HTMLButtonElement>('.account button')!.click();
    await fixture.whenStable();

    expect(session.isLoggedIn()).toBe(false);
    expect(element.querySelector('.account')?.textContent).toContain('Visiteur');
  });

  it('ouvre et ferme le menu mobile', async () => {
    const button = element.querySelector<HTMLButtonElement>('.menu-button')!;
    const nav = element.querySelector('nav')!;

    button.click();
    await fixture.whenStable();
    expect(nav.classList).toContain('open');
    expect(button.getAttribute('aria-expanded')).toBe('true');

    element.querySelector<HTMLAnchorElement>('nav a')!.click();
    await fixture.whenStable();
    expect(nav.classList).not.toContain('open');
  });
});
