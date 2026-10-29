import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';

import { environment } from '../../../../environments/environment';
import { ChangePassword } from './change-password';

describe('ChangePassword', () => {
  let fixture: ComponentFixture<ChangePassword>;
  let element: HTMLElement;
  let httpMock: HttpTestingController;
  let router: Router;
  const url = `${environment.apiUrl}/users/me/password`;

  beforeEach(async () => {
    TestBed.configureTestingModule({
      imports: [ChangePassword],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
    fixture = TestBed.createComponent(ChangePassword);
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

  it('applique la même politique de mot de passe qu’à l’inscription', async () => {
    fill('#currentPassword', 'BookHub!2026a');
    fill('#newPassword', 'faible');
    fill('#confirmPassword', 'faible');
    await submit();

    expect(element.textContent).toContain('Mot de passe trop faible');
  });

  it('change le mot de passe puis revient au profil', async () => {
    fill('#currentPassword', 'BookHub!2026a');
    fill('#newPassword', 'NouveauMotDePasse!1');
    fill('#confirmPassword', 'NouveauMotDePasse!1');
    await submit();

    const req = httpMock.expectOne(url);
    expect(req.request.body).toEqual({
      currentPassword: 'BookHub!2026a',
      newPassword: 'NouveauMotDePasse!1',
    });
    req.flush(null, { status: 204, statusText: 'No Content' });

    expect(router.navigate).toHaveBeenCalledWith(['/profile'], {
      queryParams: { passwordChanged: 1 },
    });
  });

  it('affiche le message du serveur si le mot de passe actuel est faux', async () => {
    fill('#currentPassword', 'Mauvais!2026a');
    fill('#newPassword', 'NouveauMotDePasse!1');
    fill('#confirmPassword', 'NouveauMotDePasse!1');
    await submit();

    httpMock
      .expectOne(url)
      .flush(
        { status: 400, errors: { currentPassword: 'Le mot de passe actuel est incorrect.' } },
        { status: 400, statusText: 'Bad Request' },
      );
    await fixture.whenStable();

    expect(element.textContent).toContain('Le mot de passe actuel est incorrect.');
  });
});
