import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { BookAdmin, BookDetail, BookRequest } from '../../shared/models/book';
import { Page } from '../../shared/models/page';
import { BOOKS_PER_PAGE } from './book-api';

@Injectable({ providedIn: 'root' })
export class BookAdminApi {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  getBooks(q: string, page: number): Observable<Page<BookAdmin>> {
    let params = new HttpParams().set('page', page).set('size', BOOKS_PER_PAGE);
    if (q) {
      params = params.set('q', q);
    }
    return this.http.get<Page<BookAdmin>>(`${this.apiUrl}/librarian/books`, { params });
  }

  createBook(request: BookRequest): Observable<BookDetail> {
    return this.http.post<BookDetail>(`${this.apiUrl}/books`, request);
  }

  updateBook(id: number, request: BookRequest): Observable<BookDetail> {
    return this.http.put<BookDetail>(`${this.apiUrl}/books/${id}`, request);
  }

  deleteBook(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/books/${id}`);
  }
}
