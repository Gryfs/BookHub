import { Component, inject, input } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-catalogue',
  imports: [ReactiveFormsModule],
  templateUrl: './catalogue.html',
  styleUrl: './catalogue.css',
})
export class Catalogue {
  private readonly router = inject(Router);

  readonly accountDeleted = input<string>();

  protected readonly searchForm = new FormGroup({
    query: new FormControl('', { nonNullable: true }),
  });

  onSearch(): void {
    const query = this.searchForm.controls.query.value.trim();
    if (query) {
      this.router.navigate(['/books/search'], { queryParams: { q: query } });
    }
  }
}
