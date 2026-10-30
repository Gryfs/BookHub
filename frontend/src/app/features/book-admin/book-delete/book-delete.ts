import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, input, OnInit, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { BookAdminApi } from '../../../core/api/book-admin-api';
import { BookApi } from '../../../core/api/book-api';
import { BookDetail } from '../../../shared/models/book';
import { ProblemDetail } from '../../../shared/models/problem-detail';

@Component({
  selector: 'app-book-delete',
  imports: [RouterLink],
  templateUrl: './book-delete.html',
  styleUrl: './book-delete.css',
})
export class BookDelete implements OnInit {
  private readonly bookApi = inject(BookApi);
  private readonly bookAdminApi = inject(BookAdminApi);
  private readonly router = inject(Router);

  readonly id = input.required<string>();

  protected readonly book = signal<BookDetail | null>(null);
  protected readonly errorMessage = signal('');
  protected readonly deleting = signal(false);

  ngOnInit(): void {
    this.bookApi.getBook(Number(this.id())).subscribe({
      next: (book) => this.book.set(book),
      error: () => this.errorMessage.set('Ce livre est introuvable.'),
    });
  }

  onConfirm(): void {
    this.deleting.set(true);
    this.errorMessage.set('');

    this.bookAdminApi.deleteBook(Number(this.id())).subscribe({
      next: () => this.router.navigate(['/admin/books'], { queryParams: { deleted: 1 } }),
      error: (error: HttpErrorResponse) => {
        this.deleting.set(false);
        const problem = error.error as ProblemDetail | null;
        this.errorMessage.set(
          error.status === 409 && problem?.detail
            ? problem.detail
            : 'La suppression a échoué, veuillez réessayer plus tard.',
        );
      },
    });
  }
}
