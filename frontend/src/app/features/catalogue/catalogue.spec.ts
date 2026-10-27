import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';

import { Catalogue } from './catalogue';

describe('Catalogue', () => {
  let fixture: ComponentFixture<Catalogue>;
  let element: HTMLElement;
  let router: Router;

  beforeEach(async () => {
    TestBed.configureTestingModule({
      imports: [Catalogue],
      providers: [provideRouter([])],
    });
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
    fixture = TestBed.createComponent(Catalogue);
    element = fixture.nativeElement;
    await fixture.whenStable();
  });

  function search(text: string): void {
    const input = element.querySelector('input')!;
    input.value = text;
    input.dispatchEvent(new Event('input'));
    element.querySelector('button')!.click();
  }

  it('affiche un catalogue vide', () => {
    expect(element.querySelector('.empty')?.textContent).toContain('Aucun livre');
  });

  it('envoie la recherche vers la page de recherche', () => {
    search('  camus ');

    expect(router.navigate).toHaveBeenCalledWith(['/books/search'], {
      queryParams: { q: 'camus' },
    });
  });

  it('ne fait rien si la recherche est vide', () => {
    search('   ');

    expect(router.navigate).not.toHaveBeenCalled();
  });
});
