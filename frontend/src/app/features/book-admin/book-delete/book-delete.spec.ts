import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';

import { environment } from '../../../../environments/environment';
import { BookDelete } from './book-delete';

describe('BookDelete', () => {
  let fixture: ComponentFixture<BookDelete>;
  let element: HTMLElement;
  let httpMock: HttpTestingController;
  let router: Router;
  const url = `${environment.apiUrl}/books/7`;

  beforeEach(async () => {
    TestBed.configureTestingModule({
      imports: [BookDelete],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
    fixture = TestBed.createComponent(BookDelete);
    element = fixture.nativeElement;
    fixture.componentRef.setInput('id', '7');
    fixture.detectChanges();
    httpMock.expectOne(url).flush({ id: 7, title: 'Les Misérables', author: 'Victor Hugo' });
    await fixture.whenStable();
  });

  afterEach(() => httpMock.verify());

  function confirm(): void {
    element.querySelector<HTMLButtonElement>('.btn-danger')!.click();
  }

  it('demande confirmation en rappelant le titre', () => {
    expect(element.textContent).toContain('« Les Misérables »');
    expect(element.querySelector('.alert-warning')).toBeTruthy();
  });

  it('supprime puis revient à la liste', () => {
    confirm();

    const req = httpMock.expectOne(url);
    expect(req.request.method).toBe('DELETE');
    req.flush(null, { status: 204, statusText: 'No Content' });

    expect(router.navigate).toHaveBeenCalledWith(['/admin/books'], { queryParams: { deleted: 1 } });
  });

  it('affiche le refus du serveur si le livre a été emprunté', async () => {
    confirm();

    httpMock.expectOne(url).flush(
      {
        status: 409,
        detail:
          'Impossible de supprimer « Les Misérables » : un emprunt est en cours sur ce livre.',
      },
      { status: 409, statusText: 'Conflict' },
    );
    await fixture.whenStable();

    expect(element.querySelector('.alert-error')?.textContent).toContain('un emprunt est en cours');
    expect(router.navigate).not.toHaveBeenCalled();
  });
});
