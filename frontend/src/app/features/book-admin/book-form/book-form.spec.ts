import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';

import { environment } from '../../../../environments/environment';
import { BookDetail } from '../../../shared/models/book';
import { BookForm } from './book-form';

const nineteenEightyFour: BookDetail = {
  id: 9,
  isbn: '978-2-07-061275-8',
  title: '1984',
  author: 'George Orwell',
  description: 'Big Brother vous regarde.',
  coverUrl: null,
  publicationYear: 1949,
  category: { id: 2, label: 'Science-fiction' },
  availableCopies: 1,
  totalCopies: 3,
  available: true,
  averageRating: null,
  reviews: [],
};

describe('BookForm', () => {
  let fixture: ComponentFixture<BookForm>;
  let element: HTMLElement;
  let httpMock: HttpTestingController;
  let router: Router;
  const api = environment.apiUrl;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [BookForm],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
    fixture = TestBed.createComponent(BookForm);
    element = fixture.nativeElement;
  });

  afterEach(() => httpMock.verify());

  async function start(id?: string): Promise<void> {
    if (id) {
      fixture.componentRef.setInput('id', id);
    }
    fixture.detectChanges();
    httpMock.expectOne(`${api}/categories`).flush([
      { id: 1, label: 'Roman' },
      { id: 2, label: 'Science-fiction' },
    ]);
    if (id) {
      httpMock.expectOne(`${api}/books/${id}`).flush(nineteenEightyFour);
    }
    await fixture.whenStable();
  }

  function fill(selector: string, value: string): void {
    const input = element.querySelector<HTMLInputElement | HTMLTextAreaElement>(selector)!;
    input.value = value;
    input.dispatchEvent(new Event('input'));
  }

  function selectCategory(index: number): void {
    const select = element.querySelector<HTMLSelectElement>('#categoryId')!;
    select.selectedIndex = index;
    select.dispatchEvent(new Event('change'));
  }

  async function submit(): Promise<void> {
    element.querySelector<HTMLButtonElement>('button[type=submit]')!.click();
    await fixture.whenStable();
  }

  function errors(): string[] {
    return Array.from(element.querySelectorAll('.field-error')).map((e) => e.textContent!.trim());
  }

  it('vérifie les champs obligatoires et le format de l’ISBN', async () => {
    await start();
    fill('#isbn', '123');
    await submit();

    expect(errors()).toContain('Le titre est obligatoire.');
    expect(errors()).toContain(
      "L'ISBN doit contenir 13 chiffres, éventuellement séparés par des tirets.",
    );
    expect(errors()).toContain('Choisissez une catégorie.');
  });

  it('crée un livre puis revient à la liste', async () => {
    await start();
    fill('#title', 'Germinal');
    fill('#author', 'Émile Zola');
    fill('#isbn', '978-2-07-041080-4');
    selectCategory(1);
    fill('#totalCopies', '3');
    fill('#coverUrl', 'https://covers.example.org/germinal.jpg');
    await submit();

    const req = httpMock.expectOne(`${api}/books`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      title: 'Germinal',
      author: 'Émile Zola',
      isbn: '978-2-07-041080-4',
      categoryId: 1,
      description: '',
      publicationYear: null,
      totalCopies: 3,
      coverUrl: 'https://covers.example.org/germinal.jpg',
    });
    req.flush(nineteenEightyFour);

    expect(router.navigate).toHaveBeenCalledWith(['/admin/books'], { queryParams: { saved: 1 } });
  });

  it('affiche un aperçu de la couverture', async () => {
    await start();
    fill('#coverUrl', 'https://covers.example.org/germinal.jpg');
    await fixture.whenStable();

    expect(element.querySelector('.cover-preview img')?.getAttribute('src')).toBe(
      'https://covers.example.org/germinal.jpg',
    );
  });

  it('pré-remplit le formulaire en modification', async () => {
    await start('9');

    expect(element.querySelector('h1')?.textContent).toContain('Modifier');
    expect(element.querySelector<HTMLInputElement>('#title')!.value).toBe('1984');
    expect(element.querySelector<HTMLInputElement>('#isbn')!.value).toBe('978-2-07-061275-8');
    expect(element.textContent).toContain('2 exemplaire(s) actuellement emprunté(s)');
  });

  it("refuse moins d'exemplaires que d'emprunts en cours", async () => {
    await start('9');
    fill('#totalCopies', '1');
    await submit();

    expect(errors()).toContain(
      "Il y a actuellement 2 exemplaire(s) emprunté(s) : le nombre d'exemplaires ne peut pas être inférieur.",
    );
  });

  it('affiche le refus du serveur pour un ISBN déjà utilisé', async () => {
    await start('9');
    await submit();

    httpMock
      .expectOne(`${api}/books/9`)
      .flush(
        { status: 409, detail: 'Cet ISBN est déjà utilisé par un autre livre du catalogue.' },
        { status: 409, statusText: 'Conflict' },
      );
    await fixture.whenStable();

    expect(errors()).toContain('Cet ISBN est déjà utilisé par un autre livre du catalogue.');
  });
});
