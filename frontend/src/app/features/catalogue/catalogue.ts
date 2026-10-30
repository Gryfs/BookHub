import { Component, computed, effect, inject, input, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { debounceTime, Subscription } from 'rxjs';

import { BookApi } from '../../core/api/book-api';
import { BookSearchCriteria, BookSort, BookSummary, Category } from '../../shared/models/book';
import { BookCard } from '../../shared/ui/book-card/book-card';
import { Pagination } from '../../shared/ui/pagination/pagination';

const SORTS: BookSort[] = ['TITLE', 'RATING', 'RECENT'];
const SEARCH_DELAY_MS = 300;

@Component({
  selector: 'app-catalogue',
  imports: [ReactiveFormsModule, BookCard, Pagination],
  templateUrl: './catalogue.html',
  styleUrl: './catalogue.css',
})
export class Catalogue implements OnInit {
  private readonly bookApi = inject(BookApi);
  private readonly router = inject(Router);
  private searchSubscription?: Subscription;

  readonly q = input<string>();
  readonly categoryId = input<string>();
  readonly available = input<string>();
  readonly sort = input<string>();
  readonly page = input<string>();
  readonly accountDeleted = input<string>();

  protected readonly criteria = computed<BookSearchCriteria>(() => {
    const categoryId = Number(this.categoryId());
    const page = Number(this.page());
    const sort = this.sort() as BookSort;
    return {
      q: this.q()?.trim() ?? '',
      categoryId: Number.isInteger(categoryId) && categoryId > 0 ? categoryId : null,
      available: this.available() === 'true' ? true : this.available() === 'false' ? false : null,
      sort: SORTS.includes(sort) ? sort : 'TITLE',
      page: Number.isInteger(page) && page >= 1 ? page : 1,
    };
  });

  protected readonly hasFilters = computed(() => {
    const criteria = this.criteria();
    return !!criteria.q || criteria.categoryId !== null || criteria.available !== null;
  });

  protected readonly categories = signal<Category[]>([]);
  protected readonly books = signal<BookSummary[]>([]);
  protected readonly totalBooks = signal(0);
  protected readonly totalPages = signal(0);
  protected readonly loading = signal(true);
  protected readonly errorMessage = signal('');

  protected readonly searchForm = new FormGroup({
    q: new FormControl('', { nonNullable: true }),
    categoryId: new FormControl('', { nonNullable: true }),
    available: new FormControl('', { nonNullable: true }),
    sort: new FormControl<BookSort>('TITLE', { nonNullable: true }),
  });

  constructor() {
    effect(() => {
      const criteria = this.criteria();
      this.searchForm.setValue(
        {
          q: criteria.q,
          categoryId: criteria.categoryId?.toString() ?? '',
          available: criteria.available?.toString() ?? '',
          sort: criteria.sort,
        },
        { emitEvent: false },
      );
      this.loadBooks(criteria);
    });

    this.searchForm.valueChanges
      .pipe(debounceTime(SEARCH_DELAY_MS), takeUntilDestroyed())
      .subscribe(() => this.applyFilters());
  }

  ngOnInit(): void {
    this.bookApi.getCategories().subscribe({
      next: (categories) => this.categories.set(categories),
      error: () => this.categories.set([]),
    });
  }

  applyFilters(): void {
    const { q, categoryId, available, sort } = this.searchForm.getRawValue();
    const filters = {
      q: q.trim() || null,
      categoryId: categoryId || null,
      available: available || null,
      sort: sort === 'TITLE' ? null : sort,
    };

    const current = this.criteria();
    const currentFilters = {
      q: current.q || null,
      categoryId: current.categoryId?.toString() ?? null,
      available: current.available?.toString() ?? null,
      sort: current.sort === 'TITLE' ? null : current.sort,
    };
    if (JSON.stringify(filters) === JSON.stringify(currentFilters)) {
      return;
    }

    this.router.navigate(['/books'], { queryParams: filters, replaceUrl: true });
  }

  resetFilters(): void {
    this.router.navigate(['/books']);
  }

  private loadBooks(criteria: BookSearchCriteria): void {
    this.searchSubscription?.unsubscribe();
    this.loading.set(true);
    this.errorMessage.set('');

    this.searchSubscription = this.bookApi
      .search({ ...criteria, page: criteria.page - 1 })
      .subscribe({
        next: (result) => {
          this.books.set(result.content);
          this.totalBooks.set(result.totalElements);
          this.totalPages.set(result.totalPages);
          this.loading.set(false);
        },
        error: () => {
          this.books.set([]);
          this.loading.set(false);
          this.errorMessage.set(
            'Impossible de charger le catalogue, veuillez réessayer plus tard.',
          );
        },
      });
  }
}
