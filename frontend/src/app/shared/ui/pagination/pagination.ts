import { Component, computed, input } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-pagination',
  imports: [RouterLink],
  templateUrl: './pagination.html',
  styleUrl: './pagination.css',
})
export class Pagination {
  readonly currentPage = input.required<number>();
  readonly totalPages = input.required<number>();

  protected readonly pages = computed(() => {
    const current = this.currentPage();
    const total = this.totalPages();
    const shown = new Set([1, total, current - 1, current, current + 1]);
    const numbers = [...shown].filter((page) => page >= 1 && page <= total).sort((a, b) => a - b);

    const items: (number | null)[] = [];
    numbers.forEach((page, index) => {
      if (index > 0 && page - numbers[index - 1] > 1) {
        items.push(null);
      }
      items.push(page);
    });
    return items;
  });
}
