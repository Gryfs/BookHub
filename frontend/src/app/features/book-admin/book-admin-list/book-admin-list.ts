import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';

import { BookAdminApi } from '../../../core/api/book-admin-api';
import { Session } from '../../../core/auth/session';
import { BookAdmin } from '../../../shared/models/book';
import { Pagination } from '../../../shared/ui/pagination/pagination';

@Component({
  selector: 'app-book-admin-list',
  imports: [ReactiveFormsModule, RouterLink, Pagination],
  templateUrl: './book-admin-list.html',
  styleUrl: './book-admin-list.css',
})
export class BookAdminList {
  private readonly bookAdminApi = inject(BookAdminApi);
  private readonly router = inject(Router);
  protected readonly session = inject(Session);
  private loadSubscription?: Subscription;

  readonly q = input<string>();
  readonly page = input<string>();
  readonly saved = input<string>();
  readonly deleted = input<string>();

  protected readonly currentPage = computed(() => {
    const page = Number(this.page());
    return Number.isInteger(page) && page >= 1 ? page : 1;
  });

  protected readonly books = signal<BookAdmin[]>([]);
  protected readonly totalPages = signal(0);
  protected readonly loading = signal(true);
  protected readonly errorMessage = signal('');

  protected readonly search = new FormControl('', { nonNullable: true });

  constructor() {
    effect(() => {
      const q = this.q()?.trim() ?? '';
      this.search.setValue(q);
      this.loadBooks(q, this.currentPage());
    });
  }

  onSearch(): void {
    this.router.navigate(['/admin/books'], {
      queryParams: { q: this.search.value.trim() || null },
    });
  }

  private loadBooks(q: string, page: number): void {
    this.loadSubscription?.unsubscribe();
    this.loading.set(true);
    this.errorMessage.set('');

    this.loadSubscription = this.bookAdminApi.getBooks(q, page - 1).subscribe({
      next: (result) => {
        this.books.set(result.content);
        this.totalPages.set(result.totalPages);
        this.loading.set(false);
      },
      error: () => {
        this.books.set([]);
        this.loading.set(false);
        this.errorMessage.set('Impossible de charger les livres, veuillez réessayer plus tard.');
      },
    });
  }
}
