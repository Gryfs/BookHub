import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Reservation } from './reservation-api';

@Injectable({ providedIn: 'root' })
export class LibrarianReservationApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/librarian/reservations`;

  listReady(): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(`${this.baseUrl}/ready`);
  }

  expireStale(): Observable<number> {
    return this.http.post<number>(`${this.baseUrl}/expire`, null);
  }
}
