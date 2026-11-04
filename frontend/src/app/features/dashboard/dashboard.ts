import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { DashboardApi, ReaderDashboard, ReservationSummary } from '../../core/api/dashboard-api';
import { Loan } from '../../core/api/loan-api';
import { Session } from '../../core/auth/session';

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit {
  protected readonly session = inject(Session);
  private readonly dashboardApi = inject(DashboardApi);

  protected readonly loading = signal(true);
  protected readonly errorMessage = signal('');
  protected readonly data = signal<ReaderDashboard | null>(null);

  ngOnInit(): void {
    this.dashboardApi.getReaderDashboard().subscribe({
      next: (payload) => {
        this.data.set(payload);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set('Impossible de charger votre tableau de bord.');
      },
    });
  }

  isLate(loan: Loan): boolean {
    return loan.status === 'LATE';
  }

  formatDate(iso: string): string {
    return new Date(iso + 'T12:00:00').toLocaleDateString('fr-FR');
  }

  reservationLabel(reservation: ReservationSummary): string {
    if (reservation.status === 'AVAILABLE') {
      return 'Prêt à retirer';
    }
    if (reservation.status === 'WAITING' && reservation.queuePosition != null) {
      return `En file (position ${reservation.queuePosition})`;
    }
    return reservation.status;
  }
}
