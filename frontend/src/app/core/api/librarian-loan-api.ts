import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Loan } from './loan-api';

export interface LibrarianLoan extends Loan {
  userId: number;
  userEmail: string;
  userFirstName: string;
  userLastName: string;
}

@Injectable({ providedIn: 'root' })
export class LibrarianLoanApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/librarian/loans`;

  listOutstanding(): Observable<LibrarianLoan[]> {
    return this.http.get<LibrarianLoan[]>(`${this.baseUrl}/outstanding`);
  }

  listOverdue(): Observable<LibrarianLoan[]> {
    return this.http.get<LibrarianLoan[]>(`${this.baseUrl}/overdue`);
  }

  registerReturn(loanId: number): Observable<LibrarianLoan> {
    return this.http.post<LibrarianLoan>(`${this.baseUrl}/${loanId}/return`, null);
  }

  counterLoan(userEmail: string, bookId: number): Observable<Loan> {
    return this.http.post<Loan>(`${this.baseUrl}/counter`, { userEmail, bookId });
  }
}
