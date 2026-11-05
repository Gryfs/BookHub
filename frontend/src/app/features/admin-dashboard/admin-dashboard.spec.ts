import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { environment } from '../../../environments/environment';
import { Session } from '../../core/auth/session';
import { AdminDashboardPage } from './admin-dashboard';

describe('AdminDashboardPage', () => {
  let fixture: ComponentFixture<AdminDashboardPage>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    TestBed.configureTestingModule({
      imports: [AdminDashboardPage],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: Session, useValue: { user: () => ({ firstName: 'Sophie' }) } },
      ],
    });
    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(AdminDashboardPage);
    fixture.detectChanges();
  });

  afterEach(() => httpMock.verify());

  it('loads admin dashboard and users', async () => {
    httpMock
      .expectOne(`${environment.apiUrl}/admin/dashboard`)
      .flush({
        stats: {
          bookCount: 12,
          copyCount: 20,
          activeLoans: 4,
          lateLoans: 1,
          waitingReservations: 2,
        },
        topBooks: [],
        userCount: 5,
      });
    httpMock.expectOne(`${environment.apiUrl}/admin/users`).flush([
      {
        id: 1,
        email: 'marie.lectrice@example.fr',
        firstName: 'Marie',
        lastName: 'Dupont',
        role: 'USER',
      },
    ]);
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('Administration');
    expect(fixture.nativeElement.textContent).toContain('Titres');
  });
});
