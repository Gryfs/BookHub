import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { environment } from '../../../environments/environment';
import { Session } from '../../core/auth/session';
import { Profile } from './profile';

const marieProfile = {
  id: 3,
  email: 'marie.lectrice@example.fr',
  firstName: 'Marie',
  lastName: 'Dupont',
  phone: '0611121314',
  pseudonym: 'MarieLit',
  role: 'USER',
};

describe('Profile', () => {
  let fixture: ComponentFixture<Profile>;
  let element: HTMLElement;
  let httpMock: HttpTestingController;
  const url = `${environment.apiUrl}/users/me`;

  beforeEach(async () => {
    TestBed.configureTestingModule({
      imports: [Profile],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(Profile);
    element = fixture.nativeElement;
    fixture.detectChanges();
    httpMock.expectOne(url).flush(marieProfile);
    await fixture.whenStable();
  });

  afterEach(() => httpMock.verify());

  function input(selector: string): HTMLInputElement {
    return element.querySelector<HTMLInputElement>(selector)!;
  }

  function fill(selector: string, value: string): void {
    input(selector).value = value;
    input(selector).dispatchEvent(new Event('input'));
  }

  async function submit(): Promise<void> {
    element.querySelector<HTMLButtonElement>('button[type=submit]')!.click();
    await fixture.whenStable();
  }

  it('affiche les informations personnelles', () => {
    expect(input('#firstName').value).toBe('Marie');
    expect(input('#phone').value).toBe('0611121314');
    expect(input('#pseudonym').value).toBe('MarieLit');
    expect(element.textContent).toContain('marie.lectrice@example.fr');
  });

  it('refuse un numéro de téléphone invalide', async () => {
    fill('#phone', '12');
    await submit();

    expect(element.textContent).toContain("Le numéro de téléphone n'est pas valide.");
  });

  it('enregistre les modifications et met à jour le nom dans la session', async () => {
    fill('#firstName', 'Marion');
    await submit();

    const req = httpMock.expectOne(url);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({
      firstName: 'Marion',
      lastName: 'Dupont',
      phone: '0611121314',
      pseudonym: 'MarieLit',
    });
    req.flush({ ...marieProfile, firstName: 'Marion' });
    await fixture.whenStable();

    expect(TestBed.inject(Session).user()?.firstName).toBe('Marion');
    expect(element.querySelector('.alert-success')?.textContent).toContain('enregistrées');
  });

  it('envoie un pseudonyme vide si le nom réel est choisi', async () => {
    const select = element.querySelector<HTMLSelectElement>('#displayMode')!;
    select.value = 'name';
    select.dispatchEvent(new Event('change'));
    await submit();

    const req = httpMock.expectOne(url);
    expect(req.request.body.pseudonym).toBe('');
    req.flush({ ...marieProfile, pseudonym: null });
  });
});
