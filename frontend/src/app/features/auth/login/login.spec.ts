import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';

import { environment } from '../../../../environments/environment';
import { Session } from '../../../core/auth/session';
import { Login } from './login';

describe('Login', () => {
  let fixture: ComponentFixture<Login>;
  let element: HTMLElement;
  let httpMock: HttpTestingController;
  let router: Router;

  beforeEach(async () => {
    TestBed.configureTestingModule({
      imports: [Login],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    fixture = TestBed.createComponent(Login);
    element = fixture.nativeElement;
    await fixture.whenStable();
  });

  afterEach(() => httpMock.verify());

  function fill(selector: string, value: string): void {
    const input = element.querySelector<HTMLInputElement>(selector)!;
    input.value = value;
    input.dispatchEvent(new Event('input'));
  }

  async function submit(): Promise<void> {
    element.querySelector<HTMLButtonElement>('button[type=submit]')!.click();
    await fixture.whenStable();
  }

  it("n'appelle pas l'API si le formulaire est vide", async () => {
    await submit();

    expect(element.querySelectorAll('.field-error').length).toBe(2);
  });

  it('connecte le lecteur et le redirige vers son tableau de bord', async () => {
    fill('#email', 'marie.lectrice@example.fr');
    fill('#password', 'BookHub!2026a');
    await submit();

    httpMock.expectOne(`${environment.apiUrl}/auth/login`).flush({
      token: 'jwt-123',
      user: {
        id: 3,
        email: 'marie.lectrice@example.fr',
        firstName: 'Marie',
        lastName: 'Dupont',
        pseudonym: null,
        role: 'USER',
      },
    });

    expect(TestBed.inject(Session).getToken()).toBe('jwt-123');
    expect(router.navigateByUrl).toHaveBeenCalledWith('/dashboard');
  });

  it('affiche un message générique si les identifiants sont faux', async () => {
    fill('#email', 'marie.lectrice@example.fr');
    fill('#password', 'mauvais');
    await submit();

    httpMock
      .expectOne(`${environment.apiUrl}/auth/login`)
      .flush(null, { status: 401, statusText: 'Unauthorized' });
    await fixture.whenStable();

    expect(element.querySelector('.alert-error')?.textContent).toContain(
      'Email ou mot de passe incorrect.',
    );
  });

  it('affiche le bandeau de session expirée', async () => {
    fixture.componentRef.setInput('expired', '1');
    await fixture.whenStable();

    expect(element.querySelector('.alert-warning')?.textContent).toContain('session a expiré');
  });
});
