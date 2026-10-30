import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { BookDetail, BookSearchCriteria, BookSummary, Category } from '../../shared/models/book';
import { Page } from '../../shared/models/page';

export const BOOKS_PER_PAGE = 20;

@Injectable({ providedIn: 'root' })
export class BookApi {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  getCatalogue(page: number): Observable<Page<BookSummary>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', BOOKS_PER_PAGE)
      .set('sort', 'title,asc');
    return this.http.get<Page<BookSummary>>(`${this.apiUrl}/books`, { params });
  }

  search(criteria: BookSearchCriteria): Observable<Page<BookSummary>> {
    let params = new HttpParams()
      .set('sort', criteria.sort)
      .set('page', criteria.page)
      .set('size', BOOKS_PER_PAGE);
    if (criteria.q) {
      params = params.set('q', criteria.q);
    }
    if (criteria.categoryId !== null) {
      params = params.set('categoryId', criteria.categoryId);
    }
    if (criteria.available !== null) {
      params = params.set('available', criteria.available);
    }
    return this.http.get<Page<BookSummary>>(`${this.apiUrl}/books/search`, { params });
  }

  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.apiUrl}/categories`);
  }

  getBook(id: number): Observable<BookDetail> {
    return this.http.get<BookDetail>(`${this.apiUrl}/books/${id}`);
  }
}
