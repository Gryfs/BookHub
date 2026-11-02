import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

export type ReservationStatus = 'WAITING' | 'AVAILABLE' | 'CANCELLED';

export interface Reservation {
  id: number;
  bookId: number;
  bookTitle: string;
  bookAuthor: string;
  status: ReservationStatus;
  reservationDate: string;
  pickupDeadline: string | null;
  queuePosition: number | null;
}

@Injectable({ providedIn: 'root' })
export class ReservationApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/reservations`;

  reserve(bookId: number): Observable<Reservation> {
    return this.http.post<Reservation>(this.baseUrl, { bookId });
  }

  getMyReservations(): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(`${this.baseUrl}/my`);
  }

  cancel(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
