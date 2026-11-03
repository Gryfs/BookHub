import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { LibrarianReviewApi, ModerationReview } from '../../../core/api/librarian-review-api';

@Component({
  selector: 'app-librarian-reviews',
  imports: [RouterLink],
  templateUrl: './librarian-reviews.html',
  styleUrl: './librarian-reviews.css',
})
export class LibrarianReviews implements OnInit {
  private readonly api = inject(LibrarianReviewApi);

  protected readonly reviews = signal<ModerationReview[]>([]);
  protected readonly loading = signal(true);
  protected readonly errorMessage = signal('');
  protected readonly feedback = signal('');

  ngOnInit(): void {
    this.reload();
  }

  protected remove(review: ModerationReview): void {
    const label = review.comment
      ? `« ${review.comment.slice(0, 60)}${review.comment.length > 60 ? '…' : ''} »`
      : `la note ${review.score}/5`;
    if (!confirm(`Retirer l'avis de ${review.authorLabel} sur « ${review.bookTitle} » (${label}) ?`)) {
      return;
    }
    this.feedback.set('');
    this.api.remove(review.id).subscribe({
      next: () => {
        this.feedback.set('Avis retiré.');
        this.reload();
      },
      error: () => this.feedback.set('Impossible de retirer cet avis.'),
    });
  }

  private reload(): void {
    this.loading.set(true);
    this.errorMessage.set('');
    this.api.listRecent().subscribe({
      next: (list) => {
        this.reviews.set(list);
        this.loading.set(false);
      },
      error: () => {
        this.reviews.set([]);
        this.loading.set(false);
        this.errorMessage.set('Chargement impossible.');
      },
    });
  }
}
