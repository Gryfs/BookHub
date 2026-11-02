import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { LibrarianLoan, LibrarianLoanApi } from '../../../core/api/librarian-loan-api';

@Component({
  selector: 'app-loan-overdue',
  imports: [RouterLink],
  templateUrl: './loan-overdue.html',
  styleUrl: './loan-overdue.css',
})
export class LoanOverdue implements OnInit {
  private readonly api = inject(LibrarianLoanApi);

  protected readonly loans = signal<LibrarianLoan[]>([]);
  protected readonly loading = signal(true);
  protected readonly errorMessage = signal('');

  ngOnInit(): void {
    this.api.listOverdue().subscribe({
      next: (items) => {
        this.loans.set(items);
        this.loading.set(false);
      },
      error: () => {
        this.loans.set([]);
        this.loading.set(false);
        this.errorMessage.set('Impossible de charger la liste des retards.');
      },
    });
  }

  protected readerLabel(loan: LibrarianLoan): string {
    return `${loan.userFirstName} ${loan.userLastName}`;
  }

  protected formatDate(iso: string): string {
    return new Date(iso + 'T12:00:00').toLocaleDateString('fr-FR');
  }

  protected daysLate(loan: LibrarianLoan): number {
    const due = new Date(loan.dueDate + 'T12:00:00');
    const today = new Date();
    today.setHours(12, 0, 0, 0);
    const diff = today.getTime() - due.getTime();
    return Math.max(1, Math.ceil(diff / (1000 * 60 * 60 * 24)));
  }
}
