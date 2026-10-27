import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { authInterceptor } from './auth-interceptor';
import { TokenStorage } from './token-storage';

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let token: string | null;

  beforeEach(() => {
    token = null;
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: TokenStorage, useValue: { getToken: () => token } },
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
});
