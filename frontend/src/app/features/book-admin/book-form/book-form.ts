import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, input, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { BookAdminApi } from '../../../core/api/book-admin-api';
import { BookApi } from '../../../core/api/book-api';
import { BookDetail, BookRequest, Category } from '../../../shared/models/book';
import { ProblemDetail } from '../../../shared/models/problem-detail';

const ISBN_PATTERN = /^(?=(?:\D*\d){13}$)\d+(-\d+)*$/;
const URL_PATTERN = /^https?:\/\/\S+$/;

const DEFAULT_ERRORS: Record<string, string> = {
  title: 'Le titre est obligatoire.',
  author: "L'auteur est obligatoire.",
  isbn: "L'ISBN doit contenir 13 chiffres, éventuellement séparés par des tirets.",
  categoryId: 'Choisissez une catégorie.',
  totalCopies: "Le nombre d'exemplaires doit être compris entre 0 et 1000.",
  publicationYear: "L'année de publication doit être comprise entre 1450 et 2100.",
  description: 'La description ne doit pas dépasser 2000 caractères.',
  coverUrl: "L'adresse de la couverture doit commencer par http:// ou https://.",
};

@Component({
  selector: 'app-book-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './book-form.html',
  styleUrl: './book-form.css',
})
export class BookForm implements OnInit {
  private readonly bookApi = inject(BookApi);
  private readonly bookAdminApi = inject(BookAdminApi);
  private readonly router = inject(Router);

  readonly id = input<string>();

  protected readonly isEdit = computed(() => !!this.id());
  protected readonly categories = signal<Category[]>([]);
  protected readonly borrowedCopies = signal(0);
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly errorMessage = signal('');

  protected readonly bookForm = new FormGroup({
    title: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(255)],
    }),
    author: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(255)],
    }),
    isbn: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(ISBN_PATTERN), Validators.maxLength(17)],
    }),
    categoryId: new FormControl<number | null>(null, { validators: [Validators.required] }),
    totalCopies: new FormControl<number | null>(1, {
      validators: [Validators.required, Validators.min(0), Validators.max(1000)],
    }),
    publicationYear: new FormControl<number | null>(null, {
      validators: [Validators.min(1450), Validators.max(2100)],
    }),
    description: new FormControl('', {
      nonNullable: true,
      validators: [Validators.maxLength(2000)],
    }),
    coverUrl: new FormControl('', {
      nonNullable: true,
      validators: [Validators.pattern(URL_PATTERN), Validators.maxLength(500)],
    }),
  });

  ngOnInit(): void {
    this.bookApi.getCategories().subscribe({
      next: (categories) => this.categories.set(categories),
    });

    const id = Number(this.id());
    if (this.isEdit()) {
      this.loading.set(true);
      this.bookApi.getBook(id).subscribe({
        next: (book) => {
          this.fillForm(book);
          this.loading.set(false);
        },
        error: () => {
          this.loading.set(false);
          this.errorMessage.set('Ce livre est introuvable.');
        },
      });
    }
  }

  coverPreview(): string | null {
    const control = this.bookForm.controls.coverUrl;
    return control.value && control.valid ? control.value : null;
  }

  errorFor(name: string): string | null {
    const control = this.bookForm.get(name);
    if (!control || !control.touched || control.valid) {
      return null;
    }
    if (control.hasError('server')) {
      return control.getError('server');
    }
    if (name === 'totalCopies' && control.hasError('min') && this.borrowedCopies() > 0) {
      return `Il y a actuellement ${this.borrowedCopies()} exemplaire(s) emprunté(s) : le nombre d'exemplaires ne peut pas être inférieur.`;
    }
    return DEFAULT_ERRORS[name];
  }

  onSubmit(): void {
    if (this.bookForm.invalid) {
      this.bookForm.markAllAsTouched();
      this.errorMessage.set('Veuillez corriger les erreurs ci-dessous.');
      return;
    }

    this.saving.set(true);
    this.errorMessage.set('');

    const value = this.bookForm.getRawValue();
    const request: BookRequest = {
      title: value.title,
      author: value.author,
      isbn: value.isbn,
      categoryId: value.categoryId!,
      description: value.description,
      publicationYear: value.publicationYear,
      totalCopies: value.totalCopies!,
      coverUrl: value.coverUrl,
    };

    const save = this.isEdit()
      ? this.bookAdminApi.updateBook(Number(this.id()), request)
      : this.bookAdminApi.createBook(request);

    save.subscribe({
      next: () => this.router.navigate(['/admin/books'], { queryParams: { saved: 1 } }),
      error: (error: HttpErrorResponse) => {
        this.saving.set(false);
        this.showServerErrors(error);
      },
    });
  }

  private fillForm(book: BookDetail): void {
    const borrowed = book.totalCopies - book.availableCopies;
    this.borrowedCopies.set(borrowed);
    this.bookForm.controls.totalCopies.addValidators(Validators.min(borrowed));

    this.bookForm.setValue({
      title: book.title,
      author: book.author,
      isbn: book.isbn,
      categoryId: book.category.id,
      totalCopies: book.totalCopies,
      publicationYear: book.publicationYear,
      description: book.description ?? '',
      coverUrl: book.coverUrl ?? '',
    });
  }

  private showServerErrors(error: HttpErrorResponse): void {
    const problem = error.error as ProblemDetail | null;

    if (error.status === 409) {
      this.bookForm.controls.isbn.setErrors({ server: problem?.detail });
    } else if (error.status === 400 && problem?.errors) {
      for (const [field, message] of Object.entries(problem.errors)) {
        this.bookForm.get(field)?.setErrors({ server: message });
      }
    } else if (error.status === 404) {
      this.errorMessage.set('Ce livre a été supprimé entre-temps.');
      return;
    } else {
      this.errorMessage.set('Le serveur ne répond pas, veuillez réessayer plus tard.');
      return;
    }

    this.bookForm.markAllAsTouched();
    this.errorMessage.set('Veuillez corriger les erreurs ci-dessous.');
  }
}
