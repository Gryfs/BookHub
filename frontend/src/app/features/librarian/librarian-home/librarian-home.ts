import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { DashboardApi, LibrarianDashboard } from '../../../core/api/dashboard-api';

@Component({
  selector: 'app-librarian-home',
  imports: [RouterLink],
  templateUrl: './librarian-home.html',
  styleUrl: './librarian-home.css',
})
export class LibrarianHome implements OnInit {
  private readonly dashboardApi = inject(DashboardApi);

  protected readonly statsLoading = signal(true);
  protected readonly dashboard = signal<LibrarianDashboard | null>(null);

  ngOnInit(): void {
    this.dashboardApi.getLibrarianDashboard().subscribe({
      next: (payload) => {
        this.dashboard.set(payload);
        this.statsLoading.set(false);
      },
      error: () => this.statsLoading.set(false),
    });
  }
}
