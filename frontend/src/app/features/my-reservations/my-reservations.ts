import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Reservation, ReservationApi } from '../../core/api/reservation-api';
import { ProblemDetail } from '../../shared/models/problem-detail';

@Component({
  selector: 'app-my-reservations',
  imports: [RouterLink],
  templateUrl: './my-reservations.html',
  styleUrl: './my-reservations.css',
})
export class MyReservations implements OnInit {
  private readonly api: ReservationApi = inject(ReservationApi);

  protected readonly items = signal<Reservation[]>([]);
  protected readonly loading = signal(true);
  protected readonly errorMessage = signal('');
  protected readonly rowError = signal('');
  protected readonly busyId = signal<number | null>(null);
  protected readonly cancelConfirmId = signal<number | null>(null);

  ngOnInit(): void {
    this.reload();
  }

  protected statusLabel(item: Reservation): string {
    switch (item.status) {
      case 'WAITING':
        return item.queuePosition
          ? `En file (position ${item.queuePosition})`
          : 'En file d\'attente';
      case 'AVAILABLE':
        return 'Prêt à retirer';
      default:
        return item.status;
    }
  }

  protected formatDateTime(iso: string | null): string {
    if (!iso) {
      return '';
    }
    return new Date(iso).toLocaleString('fr-FR');
  }

  protected askCancel(item: Reservation): void {
    this.rowError.set('');
    this.cancelConfirmId.set(item.id);
  }

  protected dismissCancel(): void {
    if (this.busyId() !== null) {
      return;
    }
    this.cancelConfirmId.set(null);
  }

  protected confirmCancel(item: Reservation): void {
    if (this.busyId() !== null) {
      return;
    }
    this.busyId.set(item.id);
    this.rowError.set('');
    this.api.cancel(item.id).subscribe({
      next: () => {
        this.busyId.set(null);
        this.cancelConfirmId.set(null);
        this.reload();
      },
      error: (err: HttpErrorResponse) => {
        this.busyId.set(null);
        this.rowError.set((err.error as ProblemDetail | null)?.detail ?? 'Annulation impossible.');
      },
    });
  }

  private reload(): void {
    this.loading.set(true);
    this.errorMessage.set('');
    this.api.getMyReservations().subscribe({
      next: (list: Reservation[]) => {
        this.items.set(list);
        this.loading.set(false);
      },
      error: () => {
        this.items.set([]);
        this.loading.set(false);
        this.errorMessage.set('Impossible de charger vos réservations.');
      },
    });
  }
}
