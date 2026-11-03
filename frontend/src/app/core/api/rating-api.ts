import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { BookReview } from '../../shared/models/book';
import { environment } from '../../../environments/environment';

export interface ReaderRatingContext {
  canRate: boolean;
  reason: string | null;
  myReview: BookReview | null;
}

export interface UpsertRatingPayload {
  bookId: number;
  score: number;
  comment: string | null;
}

@Injectable({ providedIn: 'root' })
export class RatingApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/ratings`;

  getContext(bookId: number): Observable<ReaderRatingContext> {
    return this.http.get<ReaderRatingContext>(`${this.baseUrl}/books/${bookId}/context`);
  }

  save(payload: UpsertRatingPayload): Observable<BookReview> {
    return this.http.put<BookReview>(this.baseUrl, payload);
  }
}
