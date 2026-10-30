import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { BookApi } from '../../core/api/book-api';
import { LoanApi } from '../../core/api/loan-api';
import { Session } from '../../core/auth/session';
import { BookDetail } from '../../shared/models/book';
import { ProblemDetail } from '../../shared/models/problem-detail';

@Component({
  selector: 'app-book-detail',
  imports: [RouterLink],
  templateUrl: './book-detail.html',
  styleUrl: './book-detail.css',
})
export class BookDetailPage {
  private readonly bookApi = inject(BookApi);
  private readonly loanApi = inject(LoanApi);
  protected readonly session = inject(Session);

  readonly id = input.required<string>();

  protected readonly book = signal<BookDetail | null>(null);
  protected readonly loading = signal(true);
  protected readonly errorMessage = signal('');
  protected readonly actionMessage = signal('');
  protected readonly actionError = signal('');
  protected readonly borrowing = signal(false);
  protected readonly loanDueDate = signal<string | null>(null);

  protected readonly bookId = computed(() => Number(this.id()));

  protected readonly loginReturnUrl = computed(() => {
    const id = this.bookId();
    return Number.isInteger(id) && id > 0 ? `/books/${id}` : '/books';
  });

  constructor() {
    effect(() => {
      const id = this.bookId();
      if (!Number.isInteger(id) || id <= 0) {
        this.loading.set(false);
        this.errorMessage.set('Identifiant de livre invalide.');
        this.book.set(null);
        return;
      }
      this.loading.set(true);
      this.errorMessage.set('');
      this.bookApi.getBook(id).subscribe({
        next: (book) => {
          this.book.set(book);
          this.loading.set(false);
        },
        error: () => {
          this.book.set(null);
          this.loading.set(false);
          this.errorMessage.set('Ce livre est introuvable ou indisponible.');
        },
      });
    });
  }

  ratingLabel(): string {
    const average = this.book()?.averageRating;
    if (average == null) {
      return 'Pas encore noté';
    }
    return `${average.toLocaleString('fr-FR', { minimumFractionDigits: 1, maximumFractionDigits: 1 })} / 5`;
  }

  borrow(): void {
    const book = this.book();
    if (!book || this.borrowing()) {
      return;
    }
    const duePreview = new Date();
    duePreview.setDate(duePreview.getDate() + 14);
    const label = duePreview.toLocaleDateString('fr-FR');
    if (!confirm(`Emprunter « ${book.title} » ?\nDate de retour prévue : ${label}`)) {
      return;
    }

    this.borrowing.set(true);
    this.actionError.set('');
    this.actionMessage.set('');
    this.loanApi.borrow(book.id).subscribe({
      next: (loan) => {
        this.borrowing.set(false);
        this.loanDueDate.set(
          new Date(loan.dueDate + 'T12:00:00').toLocaleDateString('fr-FR'),
        );
        this.actionMessage.set(`Emprunt confirmé. À rendre avant le ${this.loanDueDate()}.`);
        this.reloadBook(book.id);
      },
      error: (error: HttpErrorResponse) => {
        this.borrowing.set(false);
        const detail = (error.error as ProblemDetail | null)?.detail;
        this.actionError.set(detail ?? 'Impossible d\'emprunter ce livre.');
      },
    });
  }

  private reloadBook(id: number): void {
    this.bookApi.getBook(id).subscribe({
      next: (b) => this.book.set(b),
    });
  }
}
