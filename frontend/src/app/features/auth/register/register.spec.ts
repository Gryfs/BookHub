import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';

import { environment } from '../../../../environments/environment';
import { Register } from './register';

describe('Register', () => {
  let fixture: ComponentFixture<Register>;
  let element: HTMLElement;
  let httpMock: HttpTestingController;
  let router: Router;

  beforeEach(async () => {
    TestBed.configureTestingModule({
      imports: [Register],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
    fixture = TestBed.createComponent(Register);
    element = fixture.nativeElement;
    await fixture.whenStable();
  });

  afterEach(() => httpMock.verify());

  function fill(selector: string, value: string): void {
    const input = element.querySelector<HTMLInputElement>(selector)!;
    input.value = value;
    input.dispatchEvent(new Event('input'));
  }

  function fillValidForm(): void {
    fill('#firstName', 'Jean');
    fill('#lastName', 'Test');
    fill('#email', 'jean.test@example.fr');
    fill('#password', 'BookHub!2026a');
    fill('#confirmPassword', 'BookHub!2026a');
    element.querySelector<HTMLInputElement>('#consent')!.click();
  }

  async function submit(): Promise<void> {
    element.querySelector<HTMLButtonElement>('button[type=submit]')!.click();
    await fixture.whenStable();
  }

  function errors(): string[] {
    return Array.from(element.querySelectorAll('.field-error')).map((e) => e.textContent!.trim());
  }

  it('refuse un mot de passe trop faible sans appeler l’API', async () => {
    fillValidForm();
    fill('#password', 'motdepasse');
    fill('#confirmPassword', 'motdepasse');
    await submit();

    expect(errors()).toContain('Mot de passe trop faible (règles non respectées).');
  });

  it('vérifie que les deux mots de passe sont identiques', async () => {
    fillValidForm();
    fill('#confirmPassword', 'BookHub!2026b');
    await submit();

    expect(errors()).toContain('Les mots de passe ne correspondent pas.');
  });

  it('oblige à accepter le traitement des données', async () => {
    fillValidForm();
    element.querySelector<HTMLInputElement>('#consent')!.click();
    await submit();

    expect(errors()).toContain('Vous devez accepter le traitement de vos données.');
  });

  it('crée le compte puis redirige vers la connexion', async () => {
    fillValidForm();
    await submit();

    const req = httpMock.expectOne(`${environment.apiUrl}/auth/register`);
    expect(req.request.body).toEqual({
      firstName: 'Jean',
      lastName: 'Test',
      email: 'jean.test@example.fr',
      password: 'BookHub!2026a',
      consent: true,
    });
    req.flush({});

    expect(router.navigate).toHaveBeenCalledWith(['/login'], { queryParams: { registered: 1 } });
  });

  it('signale un email déjà utilisé', async () => {
    fillValidForm();
    await submit();

    httpMock
      .expectOne(`${environment.apiUrl}/auth/register`)
      .flush({ status: 409 }, { status: 409, statusText: 'Conflict' });
    await fixture.whenStable();

    expect(element.querySelector('.alert-error')?.textContent).toContain(
      'Impossible de créer le compte',
    );
    expect(errors()).toContain('Cette adresse email est déjà utilisée.');
  });
});
