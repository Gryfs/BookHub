import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { Session } from '../../../core/auth/session';
import { ProblemDetail } from '../../../shared/models/problem-detail';
import { ProfileApi } from '../profile-api';

@Component({
  selector: 'app-delete-account',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './delete-account.html',
  styleUrl: '../../auth/auth-form.css',
})
export class DeleteAccount {
  private readonly profileApi = inject(ProfileApi);
  private readonly session = inject(Session);
  private readonly router = inject(Router);

  protected readonly emailConfirmation = new FormControl('', { nonNullable: true });
  protected readonly errorMessage = signal('');
  protected readonly deleting = signal(false);

  isConfirmed(): boolean {
    const email = this.session.user()?.email ?? '';
    return this.emailConfirmation.value.trim().toLowerCase() === email.toLowerCase();
  }

  onDelete(): void {
    if (!this.isConfirmed()) {
      return;
    }

    this.deleting.set(true);
    this.errorMessage.set('');

    this.profileApi.deleteAccount().subscribe({
      next: () => {
        this.session.clear();
        this.router.navigate(['/books'], { queryParams: { accountDeleted: 1 } });
      },
      error: (error: HttpErrorResponse) => {
        this.deleting.set(false);
        const problem = error.error as ProblemDetail | null;
        this.errorMessage.set(
          error.status === 409 && problem?.detail
            ? problem.detail
            : 'Le serveur ne répond pas, veuillez réessayer plus tard.',
        );
      },
    });
  }
}
