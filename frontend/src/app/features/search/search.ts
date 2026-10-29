import { Component, computed, effect, inject, input, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';

import { BookApi } from '../../core/api/book-api';
import { BookSearchCriteria, BookSort, BookSummary, Category } from '../../shared/models/book';
import { Pagination } from '../../shared/ui/pagination/pagination';

const SORTS: BookSort[] = ['TITLE', 'RATING', 'RECENT'];

@Component({
  selector: 'app-search',
  imports: [ReactiveFormsModule, RouterLink, Pagination],
  templateUrl: './search.html',
  styleUrl: './search.css',
})
export class Search implements OnInit {
  private readonly bookApi = inject(BookApi);
  private readonly router = inject(Router);
  private searchSubscription?: Subscription;

  readonly q = input<string>();
  readonly categoryId = input<string>();
  readonly available = input<string>();
  readonly sort = input<string>();
  readonly page = input<string>();

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
      this.searchForm.setValue({
        q: criteria.q,
        categoryId: criteria.categoryId?.toString() ?? '',
        available: criteria.available?.toString() ?? '',
        sort: criteria.sort,
      });
      this.loadResults(criteria);
    });
  }

  ngOnInit(): void {
    this.bookApi.getCategories().subscribe({
      next: (categories) => this.categories.set(categories),
      error: () => this.categories.set([]),
    });
  }

  onSubmit(): void {
    const { q, categoryId, available, sort } = this.searchForm.getRawValue();
    this.router.navigate(['/books/search'], {
      queryParams: {
        q: q.trim() || null,
        categoryId: categoryId || null,
        available: available || null,
        sort: sort === 'TITLE' ? null : sort,
      },
    });
  }

  resetFilters(): void {
    this.router.navigate(['/books/search']);
  }

  private loadResults(criteria: BookSearchCriteria): void {
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
          this.errorMessage.set('La recherche a échoué, veuillez réessayer plus tard.');
        },
      });
  }
}
