import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { LibrarianLoan, LibrarianLoanApi } from '../../../core/api/librarian-loan-api';
import { ProblemDetail } from '../../../shared/models/problem-detail';

@Component({
  selector: 'app-loan-returns',
  imports: [RouterLink],
  templateUrl: './loan-returns.html',
  styleUrl: './loan-returns.css',
})
export class LoanReturns implements OnInit {
  private readonly api = inject(LibrarianLoanApi);

  protected readonly loans = signal<LibrarianLoan[]>([]);
  protected readonly loading = signal(true);
  protected readonly errorMessage = signal('');
  protected readonly rowMessage = signal<number | null>(null);
  protected readonly rowError = signal('');
  protected readonly busyId = signal<number | null>(null);

  ngOnInit(): void {
    this.reload();
  }

  protected readerLabel(loan: LibrarianLoan): string {
    return `${loan.userFirstName} ${loan.userLastName}`;
  }

  protected formatDate(iso: string): string {
    return new Date(iso + 'T12:00:00').toLocaleDateString('fr-FR');
  }

  protected isLate(loan: LibrarianLoan): boolean {
    return loan.status === 'LATE';
  }

  protected returnLoan(loan: LibrarianLoan): void {
    if (this.busyId() !== null) {
      return;
    }
    const label = this.readerLabel(loan);
    if (
      !confirm(
        `Confirmer le retour de « ${loan.bookTitle} » emprunté par ${label} ?`,
      )
    ) {
      return;
    }

    this.busyId.set(loan.id);
    this.rowError.set('');
    this.rowMessage.set(null);

    this.api.registerReturn(loan.id).subscribe({
      next: () => {
        this.busyId.set(null);
        this.rowMessage.set(loan.id);
        this.reload();
      },
      error: (err: HttpErrorResponse) => {
        this.busyId.set(null);
        this.rowError.set((err.error as ProblemDetail | null)?.detail ?? 'Retour impossible.');
      },
    });
  }

  private reload(): void {
    this.loading.set(true);
    this.errorMessage.set('');
    this.api.listOutstanding().subscribe({
      next: (items) => {
        this.loans.set(items);
        this.loading.set(false);
      },
      error: () => {
        this.loans.set([]);
        this.loading.set(false);
        this.errorMessage.set('Impossible de charger les emprunts en cours.');
      },
    });
  }
}
