import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { BookSummary } from '../../models/book';

@Component({
  selector: 'app-book-card',
  imports: [RouterLink],
  templateUrl: './book-card.html',
  styleUrl: './book-card.css',
})
export class BookCard {
  readonly book = input.required<BookSummary>();
}
