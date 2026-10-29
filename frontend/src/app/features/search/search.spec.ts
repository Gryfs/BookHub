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
import { Search } from './search';

const etranger: BookSummary = {
  id: 1,
  title: "L'Étranger",
  author: 'Albert Camus',
  coverUrl: null,
  available: true,
};

const dune: BookSummary = {
  id: 4,
  title: 'Dune',
  author: 'Frank Herbert',
  coverUrl: null,
  available: false,
};

function page(books: BookSummary[]): Page<BookSummary> {
  return { content: books, page: 0, size: 20, totalElements: books.length, totalPages: 1 };
}

describe('Search', () => {
  let fixture: ComponentFixture<Search>;
  let element: HTMLElement;
  let httpMock: HttpTestingController;
  let router: Router;
  const searchUrl = `${environment.apiUrl}/books/search`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [Search],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
    fixture = TestBed.createComponent(Search);
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

  it("envoie les critères de l'URL à l'API", async () => {
    const req = start({
      q: 'camus',
      categoryId: '1',
      available: 'true',
      sort: 'RATING',
      page: '2',
    });

    expect(req.request.params.get('q')).toBe('camus');
    expect(req.request.params.get('categoryId')).toBe('1');
    expect(req.request.params.get('available')).toBe('true');
    expect(req.request.params.get('sort')).toBe('RATING');
    expect(req.request.params.get('page')).toBe('1');
    expect(req.request.params.get('size')).toBe('20');
    req.flush(page([etranger]));
  });

  it('remplit le formulaire avec la recherche en cours', async () => {
    start({ q: 'camus', available: 'false' }).flush(page([]));
    await fixture.whenStable();

    expect(element.querySelector<HTMLInputElement>('#q')!.value).toBe('camus');
    expect(element.querySelector<HTMLSelectElement>('#available')!.value).toBe('false');
    expect(element.querySelectorAll('#categoryId option').length).toBe(3);
  });

  it('affiche le nombre de résultats et la disponibilité', async () => {
    start({ q: 'e' }).flush(page([etranger, dune]));
    await fixture.whenStable();

    expect(element.querySelector('.status')?.textContent).toContain('2 résultats');
    const results = element.querySelectorAll('.result');
    expect(results[0].textContent).toContain("L'Étranger");
    expect(results[0].querySelector('.badge-success')).toBeTruthy();
    expect(results[1].querySelector('.badge-danger')).toBeTruthy();
    expect(results[1].getAttribute('href')).toBe('/books/4');
  });

  it("affiche le message dédié quand il n'y a aucun résultat", async () => {
    start({ q: 'zzzz' }).flush(page([]));
    await fixture.whenStable();

    expect(element.querySelector('.empty')?.textContent).toContain(
      'Aucun livre ne correspond à votre recherche.',
    );
    element.querySelector<HTMLButtonElement>('.empty button')!.click();
    expect(router.navigate).toHaveBeenCalledWith(['/books/search']);
  });

  it('applique les filtres en les mettant dans l’URL', async () => {
    start().flush(page([etranger]));
    await fixture.whenStable();

    const input = element.querySelector<HTMLInputElement>('#q')!;
    input.value = '  harry ';
    input.dispatchEvent(new Event('input'));
    select('#categoryId', '2');
    select('#available', 'true');
    select('#sort', 'RECENT');
    element.querySelector<HTMLButtonElement>('form button[type=submit]')!.click();

    expect(router.navigate).toHaveBeenCalledWith(['/books/search'], {
      queryParams: { q: 'harry', categoryId: '2', available: 'true', sort: 'RECENT' },
    });
  });

  it("n'ajoute pas les filtres vides dans l'URL", async () => {
    start().flush(page([etranger]));
    await fixture.whenStable();

    element.querySelector<HTMLButtonElement>('form button[type=submit]')!.click();

    expect(router.navigate).toHaveBeenCalledWith(['/books/search'], {
      queryParams: { q: null, categoryId: null, available: null, sort: null },
    });
  });
});
