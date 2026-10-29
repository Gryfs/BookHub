import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';

import { BookApi } from '../../core/api/book-api';
import { BookSummary } from '../../shared/models/book';
import { BookCard } from '../../shared/ui/book-card/book-card';
import { Pagination } from '../../shared/ui/pagination/pagination';

@Component({
  selector: 'app-catalogue',
  imports: [ReactiveFormsModule, RouterLink, BookCard, Pagination],
  templateUrl: './catalogue.html',
  styleUrl: './catalogue.css',
})
export class Catalogue {
  private readonly router = inject(Router);
  private readonly bookApi = inject(BookApi);
  private loadSubscription?: Subscription;

  readonly accountDeleted = input<string>();
  readonly page = input<string>();

  protected readonly currentPage = computed(() => {
    const page = Number(this.page());
    return Number.isInteger(page) && page >= 1 ? page : 1;
  });

  protected readonly books = signal<BookSummary[]>([]);
  protected readonly totalPages = signal(0);
  protected readonly totalBooks = signal(0);
  protected readonly loading = signal(true);
  protected readonly errorMessage = signal('');

  protected readonly searchForm = new FormGroup({
    query: new FormControl('', { nonNullable: true }),
  });

  constructor() {
    effect(() => this.loadPage(this.currentPage()));
  }

  onSearch(): void {
    const query = this.searchForm.controls.query.value.trim();
    if (query) {
      this.router.navigate(['/books/search'], { queryParams: { q: query } });
    }
  }

  private loadPage(page: number): void {
    this.loadSubscription?.unsubscribe();
    this.loading.set(true);
    this.errorMessage.set('');

    this.loadSubscription = this.bookApi.getCatalogue(page - 1).subscribe({
      next: (result) => {
        this.books.set(result.content);
        this.totalPages.set(result.totalPages);
        this.totalBooks.set(result.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.books.set([]);
        this.loading.set(false);
        this.errorMessage.set('Impossible de charger le catalogue, veuillez réessayer plus tard.');
      },
    });
  }
}
