import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';

import { environment } from '../../../environments/environment';
import { authInterceptor } from './auth-interceptor';
import { Session } from './session';

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let token: string | null;
  let session: { getToken: () => string | null; clear: ReturnType<typeof vi.fn> };
  let router: { navigate: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    token = null;
    session = { getToken: () => token, clear: vi.fn() };
    router = { navigate: vi.fn() };
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: Session, useValue: session },
        { provide: Router, useValue: router },
      ],
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it("ajoute le jeton aux appels vers l'API", () => {
    token = 'jwt-123';

    http.get(`${environment.apiUrl}/books`).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/books`);
    expect(req.request.headers.get('Authorization')).toBe('Bearer jwt-123');
    req.flush([]);
  });

  it("n'ajoute rien sans jeton", () => {
    http.get(`${environment.apiUrl}/books`).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/books`);
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush([]);
  });

  it("n'envoie jamais le jeton à un autre domaine", () => {
    token = 'jwt-123';

    http.get('https://covers.example.org/img.jpg').subscribe();

    const req = httpMock.expectOne('https://covers.example.org/img.jpg');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush(null);
  });

  it('déconnecte et redirige vers la connexion si le jeton est refusé', () => {
    token = 'jwt-expire';

    http.get(`${environment.apiUrl}/auth/me`).subscribe({ error: () => {} });

    httpMock
      .expectOne(`${environment.apiUrl}/auth/me`)
      .flush(null, { status: 401, statusText: 'Unauthorized' });
    expect(session.clear).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/login'], { queryParams: { expired: 1 } });
  });
});
