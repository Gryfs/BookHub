import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';

import { environment } from '../../../environments/environment';
import { BookSummary } from '../../shared/models/book';
import { Page } from '../../shared/models/page';
import { Catalogue } from './catalogue';

function bookPage(books: BookSummary[], page = 0, totalPages = 1): Page<BookSummary> {
  return { content: books, page, size: 20, totalElements: books.length, totalPages };
}

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

describe('Catalogue', () => {
  let fixture: ComponentFixture<Catalogue>;
  let element: HTMLElement;
  let router: Router;
  let httpMock: HttpTestingController;
  const url = `${environment.apiUrl}/books`;

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

  async function respond(result: Page<BookSummary>, page = 0): Promise<void> {
    fixture.detectChanges();
    const req = httpMock.expectOne((r) => r.url === url);
    expect(req.request.params.get('page')).toBe(String(page));
    expect(req.request.params.get('size')).toBe('20');
    req.flush(result);
    await fixture.whenStable();
  }

  function search(text: string): void {
    const input = element.querySelector<HTMLInputElement>('input[type=search]')!;
    input.value = text;
    input.dispatchEvent(new Event('input'));
    element.querySelector<HTMLButtonElement>('form button')!.click();
  }

  it('affiche les livres avec titre, auteur et disponibilité', async () => {
    await respond(bookPage([dune, fondation]));

    const cards = element.querySelectorAll('app-book-card');
    expect(cards.length).toBe(2);
    expect(cards[0].textContent).toContain('Dune');
    expect(cards[0].textContent).toContain('Frank Herbert');
    expect(cards[0].querySelector('.badge-danger')?.textContent).toContain('Indisponible');
    expect(cards[1].querySelector('.badge-success')?.textContent).toContain('Disponible');
  });

  it('affiche la couverture ou une image par défaut', async () => {
    await respond(bookPage([dune, fondation]));

    const cards = element.querySelectorAll('app-book-card');
    expect(cards[0].querySelector('.cover-placeholder')).toBeTruthy();
    expect(cards[1].querySelector('img')?.getAttribute('src')).toBe(fondation.coverUrl);
  });

  it('ouvre la fiche détail au clic', async () => {
    await respond(bookPage([dune]));

    const link = element.querySelector<HTMLAnchorElement>('app-book-card a')!;
    expect(link.getAttribute('href')).toBe('/books/4');
  });

  it('charge la page demandée dans l’URL', async () => {
    fixture.componentRef.setInput('page', '3');

    await respond(bookPage([dune], 2, 5), 2);

    expect(element.querySelector('.current')?.textContent).toContain('3');
  });

  it('affiche un message si le catalogue est vide', async () => {
    await respond(bookPage([], 0, 0));

    expect(element.querySelector('.empty')?.textContent).toContain('Aucun livre');
  });

  it("affiche une erreur si l'API ne répond pas", async () => {
    fixture.detectChanges();
    httpMock.expectOne((r) => r.url === url).flush(null, { status: 500, statusText: 'Error' });
    await fixture.whenStable();

    expect(element.querySelector('.alert-error')?.textContent).toContain('Impossible de charger');
  });

  it('envoie la recherche vers la page de recherche', async () => {
    await respond(bookPage([dune]));

    search('  camus ');

    expect(router.navigate).toHaveBeenCalledWith(['/books/search'], {
      queryParams: { q: 'camus' },
    });
  });
});
