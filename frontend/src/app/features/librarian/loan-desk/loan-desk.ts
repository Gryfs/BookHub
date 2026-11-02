import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { LibrarianLoanApi } from '../../../core/api/librarian-loan-api';
import { ProblemDetail } from '../../../shared/models/problem-detail';

@Component({
  selector: 'app-loan-desk',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './loan-desk.html',
  styleUrl: './loan-desk.css',
})
export class LoanDesk {
  private readonly api = inject(LibrarianLoanApi);

  protected readonly submitting = signal(false);
  protected readonly successMessage = signal('');
  protected readonly errorMessage = signal('');

  protected readonly form = new FormGroup({
    userEmail: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email],
    }),
    bookId: new FormControl<number | null>(null, {
      validators: [Validators.required, Validators.min(1)],
    }),
  });

  protected submit(): void {
    this.successMessage.set('');
    this.errorMessage.set('');
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }

    const { userEmail, bookId } = this.form.getRawValue();
    if (bookId === null) {
      return;
    }

    this.submitting.set(true);
    this.api.counterLoan(userEmail.trim(), bookId).subscribe({
      next: (loan) => {
        this.submitting.set(false);
        this.successMessage.set(
          `Emprunt enregistré pour « ${loan.bookTitle} » (retour prévu le ${this.formatDate(loan.dueDate)}).`,
        );
        this.form.reset({ userEmail: '', bookId: null });
      },
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        this.errorMessage.set((err.error as ProblemDetail | null)?.detail ?? 'Emprunt refusé.');
      },
    });
  }

  private formatDate(iso: string): string {
    return new Date(iso + 'T12:00:00').toLocaleDateString('fr-FR');
  }
}
