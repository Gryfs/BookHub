import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { LoanApi, Loan } from '../../core/api/loan-api';

@Component({
  selector: 'app-my-loans',
  imports: [RouterLink],
  templateUrl: './my-loans.html',
  styleUrl: './my-loans.css',
})
export class MyLoans implements OnInit {
  private readonly loanApi = inject(LoanApi);

  protected readonly current = signal<Loan[]>([]);
  protected readonly history = signal<Loan[]>([]);
  protected readonly tab = signal<'current' | 'history'>('current');
  protected readonly loading = signal(true);
  protected readonly errorMessage = signal('');

  ngOnInit(): void {
    this.loanApi.getMyLoans().subscribe({
      next: (result) => {
        this.current.set(result.current);
        this.history.set(result.history);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set('Impossible de charger vos emprunts.');
      },
    });
  }

  isLate(loan: Loan): boolean {
    return loan.status === 'LATE';
  }

  formatDate(iso: string): string {
    return new Date(iso + 'T12:00:00').toLocaleDateString('fr-FR');
  }
}
