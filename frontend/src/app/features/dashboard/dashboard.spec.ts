import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { environment } from '../../../environments/environment';
import { Session } from '../../core/auth/session';
import { Dashboard } from './dashboard';

describe('Dashboard', () => {
  let fixture: ComponentFixture<Dashboard>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    TestBed.configureTestingModule({
      imports: [Dashboard],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: Session, useValue: { user: () => ({ firstName: 'Marie' }) } },
      ],
    });
    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(Dashboard);
    fixture.detectChanges();
  });

  afterEach(() => httpMock.verify());

  it('loads reader dashboard', async () => {
    const req = httpMock.expectOne(`${environment.apiUrl}/dashboard/reader`);
    expect(req.request.method).toBe('GET');
    req.flush({
      currentLoans: [],
      reservations: [],
      hasLateLoan: false,
      recentlyReturned: [],
    });
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('Votre espace lecteur');
  });
});
