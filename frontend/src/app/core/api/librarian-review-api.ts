import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

export interface ModerationReview {
  id: number;
  bookId: number;
  bookTitle: string;
  authorLabel: string;
  score: number;
  comment: string | null;
}

@Injectable({ providedIn: 'root' })
export class LibrarianReviewApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/librarian/reviews`;

  listRecent(limit = 40): Observable<ModerationReview[]> {
    return this.http.get<ModerationReview[]>(this.baseUrl, { params: { limit } });
  }

  remove(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
