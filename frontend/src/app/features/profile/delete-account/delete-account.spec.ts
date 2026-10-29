import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';

import { environment } from '../../../../environments/environment';
import { Session } from '../../../core/auth/session';
import { DeleteAccount } from './delete-account';

describe('DeleteAccount', () => {
  let fixture: ComponentFixture<DeleteAccount>;
  let element: HTMLElement;
  let httpMock: HttpTestingController;
  let router: Router;
  let session: Session;
  const url = `${environment.apiUrl}/users/me`;

  beforeEach(async () => {
    TestBed.configureTestingModule({
      imports: [DeleteAccount],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
    session = TestBed.inject(Session);
    session.start('jwt-123', {
      id: 3,
      email: 'marie.lectrice@example.fr',
      firstName: 'Marie',
      lastName: 'Dupont',
      role: 'USER',
    });
    fixture = TestBed.createComponent(DeleteAccount);
    element = fixture.nativeElement;
    await fixture.whenStable();
  });

  afterEach(() => httpMock.verify());

  function deleteButton(): HTMLButtonElement {
    return element.querySelector<HTMLButtonElement>('.btn-danger')!;
  }

  async function typeEmail(value: string): Promise<void> {
    const input = element.querySelector<HTMLInputElement>('#emailConfirmation')!;
    input.value = value;
    input.dispatchEvent(new Event('input'));
    await fixture.whenStable();
  }

  it("bloque le bouton tant que l'email ne correspond pas", async () => {
    expect(deleteButton().disabled).toBe(true);

    await typeEmail('autre@example.fr');

    expect(deleteButton().disabled).toBe(true);
  });

  it('supprime le compte puis déconnecte', async () => {
    await typeEmail('Marie.Lectrice@example.fr');
    deleteButton().click();

    const req = httpMock.expectOne(url);
    expect(req.request.method).toBe('DELETE');
    req.flush(null, { status: 204, statusText: 'No Content' });

    expect(session.isLoggedIn()).toBe(false);
    expect(router.navigate).toHaveBeenCalledWith(['/books'], {
      queryParams: { accountDeleted: 1 },
    });
  });

  it('affiche le refus si un emprunt est en cours', async () => {
    await typeEmail('marie.lectrice@example.fr');
    deleteButton().click();

    httpMock.expectOne(url).flush(
      {
        status: 409,
        detail: "Vous ne pouvez pas supprimer votre compte tant qu'un emprunt est en cours.",
      },
      { status: 409, statusText: 'Conflict' },
    );
    await fixture.whenStable();

    expect(session.isLoggedIn()).toBe(true);
    expect(element.querySelector('.alert-error')?.textContent).toContain('emprunt est en cours');
  });
});
