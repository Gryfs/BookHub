import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { Header } from './header';

describe('Header', () => {
  let fixture: ComponentFixture<Header>;
  let element: HTMLElement;

  beforeEach(async () => {
    TestBed.configureTestingModule({
      imports: [Header],
      providers: [provideRouter([{ path: '**', children: [] }])],
    });
    fixture = TestBed.createComponent(Header);
    element = fixture.nativeElement;
    await fixture.whenStable();
  });

  it('affiche les liens du visiteur', () => {
    const links = Array.from(element.querySelectorAll('nav a')).map((a) => a.textContent?.trim());

    expect(links).toEqual(['Catalogue', 'Recherche', 'Connexion']);
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
