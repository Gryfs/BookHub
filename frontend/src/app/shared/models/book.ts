export interface BookSummary {
  id: number;
  title: string;
  author: string;
  coverUrl: string | null;
  available: boolean;
}

export interface Category {
  id: number;
  label: string;
}

export type BookSort = 'TITLE' | 'RATING' | 'RECENT';

export interface BookSearchCriteria {
  q: string;
  categoryId: number | null;
  available: boolean | null;
  sort: BookSort;
  page: number;
}
