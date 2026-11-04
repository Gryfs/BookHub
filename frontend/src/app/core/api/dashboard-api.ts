import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Loan } from './loan-api';

export interface ReservationSummary {
  id: number;
  bookId: number;
  bookTitle: string;
  bookAuthor: string;
  status: 'WAITING' | 'AVAILABLE' | 'CANCELLED' | 'EXPIRED' | 'FULFILLED';
  reservationDate: string;
  pickupDeadline: string | null;
  queuePosition: number | null;
}

export interface ReaderDashboard {
  currentLoans: Loan[];
  reservations: ReservationSummary[];
  hasLateLoan: boolean;
  recentlyReturned: Loan[];
}

export interface LibraryStats {
  bookCount: number;
  copyCount: number;
  activeLoans: number;
  lateLoans: number;
  waitingReservations: number;
}

export interface TopBookStat {
  bookId: number;
  title: string;
  author: string;
  loanCount: number;
}

export interface LibrarianDashboard {
  stats: LibraryStats;
  topBooks: TopBookStat[];
}

export interface AdminDashboard {
  stats: LibraryStats;
  topBooks: TopBookStat[];
  userCount: number;
}

export type UserRole = 'USER' | 'LIBRARIAN' | 'ADMIN';

export interface AdminUser {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: UserRole;
}

@Injectable({ providedIn: 'root' })
export class DashboardApi {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;

  getReaderDashboard(): Observable<ReaderDashboard> {
    return this.http.get<ReaderDashboard>(`${this.base}/dashboard/reader`);
  }

  getLibrarianDashboard(): Observable<LibrarianDashboard> {
    return this.http.get<LibrarianDashboard>(`${this.base}/librarian/dashboard`);
  }

  getAdminDashboard(): Observable<AdminDashboard> {
    return this.http.get<AdminDashboard>(`${this.base}/admin/dashboard`);
  }

  listAdminUsers(): Observable<AdminUser[]> {
    return this.http.get<AdminUser[]>(`${this.base}/admin/users`);
  }

  updateUserRole(userId: number, role: UserRole): Observable<AdminUser> {
    return this.http.put<AdminUser>(`${this.base}/admin/users/${userId}/role`, { role });
  }
}
