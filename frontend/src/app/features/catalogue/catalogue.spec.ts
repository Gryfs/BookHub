import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
  TestRequest,
} from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';

import { environment } from '../../../environments/environment';
import { BookSummary } from '../../shared/models/book';
import { Page } from '../../shared/models/page';
import { Catalogue } from './catalogue';

const dune: BookSummary = {
  id: 4,
  title: 'Dune',
  author: 'Frank Herbert',
  coverUrl: null,
  available: false,
};

const fondation: BookSummary = {
  id: 3,
  title: 'Fondation',
  author: 'Isaac Asimov',
  coverUrl: 'https://covers.example.org/fondation.jpg',
  available: true,
};

function bookPage(books: BookSummary[], page = 0, totalPages = 1): Page<BookSummary> {
  return { content: books, page, size: 20, totalElements: books.length, totalPages };
}

describe('Catalogue', () => {
  let fixture: ComponentFixture<Catalogue>;
  let element: HTMLElement;
  let router: Router;
  let httpMock: HttpTestingController;
  const searchUrl = `${environment.apiUrl}/books/search`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [Catalogue],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    router = TestBed.inject(Router);
    httpMock = TestBed.inject(HttpTestingController);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
    fixture = TestBed.createComponent(Catalogue);
    element = fixture.nativeElement;
  });

  afterEach(() => httpMock.verify());

  function start(queryParams: Record<string, string> = {}): TestRequest {
    for (const [name, value] of Object.entries(queryParams)) {
      fixture.componentRef.setInput(name, value);
    }
    fixture.detectChanges();
    httpMock.expectOne(`${environment.apiUrl}/categories`).flush([
      { id: 1, label: 'Roman' },
      { id: 2, label: 'Science-fiction' },
    ]);
    return httpMock.expectOne((req) => req.url === searchUrl);
  }

  function select(selector: string, value: string): void {
    const field = element.querySelector<HTMLSelectElement>(selector)!;
    field.value = value;
    field.dispatchEvent(new Event('change'));
  }

  function type(text: string): void {
    const input = element.querySelector<HTMLInputElement>('#q')!;
    input.value = text;
    input.dispatchEvent(new Event('input'));
  }

  function pause(): Promise<void> {
    return new Promise((resolve) => setTimeout(resolve, 350));
  }

  it('affiche tous les livres, 20 par page, triés par titre', async () => {
    const req = start();
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('size')).toBe('20');
    expect(req.request.params.get('sort')).toBe('TITLE');
    expect(req.request.params.has('q')).toBe(false);
    req.flush(bookPage([dune, fondation]));
    await fixture.whenStable();

    const cards = element.querySelectorAll('app-book-card');
    expect(cards.length).toBe(2);
    expect(cards[0].textContent).toContain('Dune');
    expect(cards[0].textContent).toContain('Frank Herbert');
    expect(cards[0].querySelector('.badge-danger')?.textContent).toContain('Indisponible');
    expect(cards[1].querySelector('.badge-success')?.textContent).toContain('Disponible');
    expect(element.querySelector('.status')?.textContent).toContain('2 livres');
  });

  it('affiche la couverture ou une image par défaut', async () => {
    start().flush(bookPage([dune, fondation]));
    await fixture.whenStable();

    const cards = element.querySelectorAll('app-book-card');
    expect(cards[0].querySelector('.cover-placeholder')).toBeTruthy();
    expect(cards[1].querySelector('img')?.getAttribute('src')).toBe(fondation.coverUrl);
  });

  it('ouvre la fiche détail au clic', async () => {
    start().flush(bookPage([dune]));
    await fixture.whenStable();

    expect(element.querySelector('app-book-card a')!.getAttribute('href')).toBe('/books/4');
  });

  it("envoie à l'API la recherche et les filtres de l'URL", () => {
    const req = start({
      q: 'camus',
      categoryId: '1',
      available: 'true',
      sort: 'RATING',
      page: '3',
    });

    expect(req.request.params.get('q')).toBe('camus');
    expect(req.request.params.get('categoryId')).toBe('1');
    expect(req.request.params.get('available')).toBe('true');
    expect(req.request.params.get('sort')).toBe('RATING');
    expect(req.request.params.get('page')).toBe('2');
    req.flush(bookPage([]));
  });

  it('remplit les filtres avec la recherche en cours', async () => {
    start({ q: 'camus', available: 'false' }).flush(bookPage([]));
    await fixture.whenStable();

    expect(element.querySelector<HTMLInputElement>('#q')!.value).toBe('camus');
    expect(element.querySelector<HTMLSelectElement>('#available')!.value).toBe('false');
    expect(element.querySelectorAll('#categoryId option').length).toBe(3);
    expect(element.querySelector('.subtitle')?.textContent).toContain('« camus »');
  });

  it('met à jour la recherche pendant la saisie, après une courte pause', async () => {
    start().flush(bookPage([dune]));
    await fixture.whenStable();

    type('h');
    type('ha');
    type('  harry ');
    expect(router.navigate).not.toHaveBeenCalled();

    await pause();

    expect(router.navigate).toHaveBeenCalledTimes(1);
    expect(router.navigate).toHaveBeenCalledWith(['/books'], {
      queryParams: { q: 'harry', categoryId: null, available: null, sort: null },
      replaceUrl: true,
    });
  });

  it('applique les filtres dès qu’on les choisit', async () => {
    start().flush(bookPage([dune]));
    await fixture.whenStable();

    select('#categoryId', '2');
    select('#available', 'true');
    select('#sort', 'RECENT');
    await pause();

    expect(router.navigate).toHaveBeenCalledWith(['/books'], {
      queryParams: { q: null, categoryId: '2', available: 'true', sort: 'RECENT' },
      replaceUrl: true,
    });
  });

  it("ne relance rien si la recherche n'a pas vraiment changé", async () => {
    start({ q: 'dune' }).flush(bookPage([dune]));
    await fixture.whenStable();

    type('dune  ');
    await pause();

    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('applique tout de suite avec la touche Entrée', async () => {
    start().flush(bookPage([dune]));
    await fixture.whenStable();

    type('dune');
    element.querySelector('form')!.dispatchEvent(new Event('submit'));

    expect(router.navigate).toHaveBeenCalledWith(['/books'], {
      queryParams: { q: 'dune', categoryId: null, available: null, sort: null },
      replaceUrl: true,
    });
  });

  it('garde les résultats affichés pendant le chargement suivant', async () => {
    start().flush(bookPage([dune]));
    await fixture.whenStable();

    fixture.componentRef.setInput('q', 'fondation');
    await fixture.whenStable();

    expect(element.querySelector('.results')?.classList).toContain('refreshing');
    expect(element.querySelectorAll('app-book-card').length).toBe(1);
    httpMock.expectOne((req) => req.url === searchUrl).flush(bookPage([fondation]));
  });

  it('affiche le message dédié quand la recherche ne donne rien', async () => {
    start({ q: 'zzzz' }).flush(bookPage([]));
    await fixture.whenStable();

    expect(element.querySelector('.empty')?.textContent).toContain(
      'Aucun livre ne correspond à votre recherche.',
    );
    element.querySelector<HTMLButtonElement>('.empty button')!.click();
    expect(router.navigate).toHaveBeenCalledWith(['/books']);
  });

  it('propose de réinitialiser seulement si un filtre est actif', async () => {
    start().flush(bookPage([dune]));
    await fixture.whenStable();
    expect(element.textContent).not.toContain('Réinitialiser les filtres');

    fixture.componentRef.setInput('available', 'true');
    await fixture.whenStable();
    httpMock.expectOne((req) => req.url === searchUrl).flush(bookPage([dune]));
    await fixture.whenStable();

    expect(element.querySelector('.filters')?.textContent).toContain('Réinitialiser les filtres');
  });

  it("affiche une erreur si l'API ne répond pas", async () => {
    start().flush(null, { status: 500, statusText: 'Error' });
    await fixture.whenStable();

    expect(element.querySelector('.alert-error')?.textContent).toContain('Impossible de charger');
  });

  it('affiche la pagination quand il y a plusieurs pages', async () => {
    start({ page: '2' }).flush(bookPage([dune], 1, 3));
    await fixture.whenStable();

    expect(element.querySelector('.current')?.textContent).toContain('2');
    expect(element.querySelector('.status')?.textContent).toContain('page 2 sur 3');
  });
});
