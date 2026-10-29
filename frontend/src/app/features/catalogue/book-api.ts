import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { BookSummary } from '../../shared/models/book';
import { Page } from '../../shared/models/page';

export const BOOKS_PER_PAGE = 20;

@Injectable({ providedIn: 'root' })
export class BookApi {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/books`;

  getCatalogue(page: number): Observable<Page<BookSummary>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', BOOKS_PER_PAGE)
      .set('sort', 'title,asc');
    return this.http.get<Page<BookSummary>>(this.apiUrl, { params });
  }
}
