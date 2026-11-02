import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { LibrarianReservationApi } from '../../../core/api/librarian-reservation-api';
import { Reservation } from '../../../core/api/reservation-api';

@Component({
  selector: 'app-librarian-reservations',
  imports: [RouterLink],
  templateUrl: './librarian-reservations.html',
  styleUrl: './librarian-reservations.css',
})
export class LibrarianReservations implements OnInit {
  private readonly api = inject(LibrarianReservationApi);

  protected readonly ready = signal<Reservation[]>([]);
  protected readonly loading = signal(true);
  protected readonly errorMessage = signal('');
  protected readonly maintenanceMessage = signal('');

  ngOnInit(): void {
    this.reload();
  }

  protected formatDateTime(iso: string | null): string {
    return iso ? new Date(iso).toLocaleString('fr-FR') : '';
  }

  protected expireStale(): void {
    this.maintenanceMessage.set('');
    this.api.expireStale().subscribe({
      next: (count) => {
        this.maintenanceMessage.set(`${count} retrait(s) expiré(s) traité(s).`);
        this.reload();
      },
      error: () => this.maintenanceMessage.set('Échec du traitement.'),
    });
  }

  private reload(): void {
    this.loading.set(true);
    this.errorMessage.set('');
    this.api.listReady().subscribe({
      next: (list) => {
        this.ready.set(list);
        this.loading.set(false);
      },
      error: () => {
        this.ready.set([]);
        this.loading.set(false);
        this.errorMessage.set('Impossible de charger les retraits.');
      },
    });
  }
}
