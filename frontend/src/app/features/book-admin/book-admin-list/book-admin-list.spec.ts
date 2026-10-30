import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';

import { environment } from '../../../../environments/environment';
import { Session } from '../../../core/auth/session';
import { BookAdmin } from '../../../shared/models/book';
import { Page } from '../../../shared/models/page';
import { Role } from '../../../shared/models/user';
import { BookAdminList } from './book-admin-list';

const petitPrince: BookAdmin = {
  id: 2,
  isbn: '978-2-07-040850-4',
  title: 'Le Petit Prince',
  author: 'Antoine de Saint-Exupéry',
  totalCopies: 5,
  availableCopies: 3,
};

function page(books: BookAdmin[]): Page<BookAdmin> {
  return { content: books, page: 0, size: 20, totalElements: books.length, totalPages: 1 };
}

describe('BookAdminList', () => {
  let fixture: ComponentFixture<BookAdminList>;
  let element: HTMLElement;
  let httpMock: HttpTestingController;
  let router: Router;
  const url = `${environment.apiUrl}/librarian/books`;

  function setup(role: Role): void {
    TestBed.configureTestingModule({
      imports: [BookAdminList],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    TestBed.inject(Session).start('jwt', {
      id: 2,
      email: 'test@example.fr',
      firstName: 'Test',
      lastName: 'Test',
      role,
    });
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
    fixture = TestBed.createComponent(BookAdminList);
    element = fixture.nativeElement;
  }

  afterEach(() => httpMock.verify());

  async function load(books: BookAdmin[]): Promise<void> {
    fixture.detectChanges();
    httpMock.expectOne((req) => req.url === url).flush(page(books));
    await fixture.whenStable();
  }

  it('affiche ISBN, titre, auteur et exemplaires', async () => {
    setup('LIBRARIAN');
    await load([petitPrince]);

    const cells = Array.from(element.querySelectorAll('tbody td')).map((td) =>
      td.textContent!.trim(),
    );
    expect(cells[0]).toBe('978-2-07-040850-4');
    expect(cells[1]).toBe('Le Petit Prince');
    expect(cells[3]).toContain('5');
    expect(cells[3]).toContain('(3 dispo)');
  });

  it('propose Modifier mais pas Supprimer au bibliothécaire', async () => {
    setup('LIBRARIAN');
    await load([petitPrince]);

    const links = Array.from(element.querySelectorAll('.actions a')).map((a) =>
      a.textContent!.trim(),
    );
    expect(links).toEqual(['Modifier']);
    expect(element.querySelector('.actions a')!.getAttribute('href')).toBe('/admin/books/2/edit');
  });

  it("propose aussi Supprimer à l'administrateur", async () => {
    setup('ADMIN');
    await load([petitPrince]);

    const deleteLink = element.querySelector<HTMLAnchorElement>('.actions .btn-danger')!;
    expect(deleteLink.textContent!.trim()).toBe('Supprimer');
    expect(deleteLink.getAttribute('href')).toBe('/admin/books/2/delete');
  });

  it('transmet la recherche dans l’URL', async () => {
    setup('LIBRARIAN');
    await load([petitPrince]);

    const input = element.querySelector<HTMLInputElement>('input[type=search]')!;
    input.value = ' prince ';
    input.dispatchEvent(new Event('input'));
    element.querySelector<HTMLButtonElement>('form button')!.click();

    expect(router.navigate).toHaveBeenCalledWith(['/admin/books'], {
      queryParams: { q: 'prince' },
    });
  });

  it('affiche la confirmation après un enregistrement', async () => {
    setup('LIBRARIAN');
    fixture.componentRef.setInput('saved', '1');
    await load([petitPrince]);

    expect(element.querySelector('.alert-success')?.textContent).toContain('enregistré');
  });
});
