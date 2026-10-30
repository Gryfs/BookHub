import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

export interface Loan {
  id: number;
  bookId: number;
  bookTitle: string;
  bookAuthor: string;
  loanDate: string;
  dueDate: string;
  returnDate: string | null;
  status: 'ACTIVE' | 'LATE' | 'RETURNED';
}

export interface MyLoans {
  current: Loan[];
  history: Loan[];
}

@Injectable({ providedIn: 'root' })
export class LoanApi {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/loans`;

  borrow(bookId: number): Observable<Loan> {
    return this.http.post<Loan>(this.apiUrl, { bookId });
  }

  getMyLoans(): Observable<MyLoans> {
    return this.http.get<MyLoans>(`${this.apiUrl}/my`);
  }
}
