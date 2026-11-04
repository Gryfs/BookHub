import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import {
  AdminDashboard,
  AdminUser,
  DashboardApi,
  UserRole,
} from '../../core/api/dashboard-api';
import { Session } from '../../core/auth/session';

@Component({
  selector: 'app-admin-dashboard',
  imports: [RouterLink],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css',
})
export class AdminDashboardPage implements OnInit {
  protected readonly session = inject(Session);
  private readonly dashboardApi = inject(DashboardApi);

  protected readonly loading = signal(true);
  protected readonly errorMessage = signal('');
  protected readonly dashboard = signal<AdminDashboard | null>(null);
  protected readonly users = signal<AdminUser[]>([]);
  protected readonly roleError = signal('');
  protected readonly roles: UserRole[] = ['USER', 'LIBRARIAN', 'ADMIN'];

  ngOnInit(): void {
    this.dashboardApi.getAdminDashboard().subscribe({
      next: (payload) => {
        this.dashboard.set(payload);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set('Impossible de charger le tableau de bord admin.');
      },
    });

    this.dashboardApi.listAdminUsers().subscribe({
      next: (list) => this.users.set(list),
      error: () => this.roleError.set('Impossible de charger la liste des utilisateurs.'),
    });
  }

  onRoleChange(user: AdminUser, role: UserRole): void {
    if (user.role === role) {
      return;
    }
    this.roleError.set('');
    this.dashboardApi.updateUserRole(user.id, role).subscribe({
      next: (updated) => {
        this.users.update((list) => list.map((u) => (u.id === updated.id ? updated : u)));
      },
      error: (err: HttpErrorResponse) => {
        const msg =
          typeof err.error?.detail === 'string'
            ? err.error.detail
            : 'Modification du rôle impossible.';
        this.roleError.set(msg);
        this.users.set([...this.users()]);
      },
    });
  }
}
