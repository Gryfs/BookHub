export interface BookSummary {
  id: number;
  title: string;
  author: string;
  coverUrl: string | null;
  available: boolean;
}

export interface BookReview {
  id: number;
  authorLabel: string;
  score: number;
  comment: string | null;
}

export interface BookDetail {
  id: number;
  isbn: string;
  title: string;
  author: string;
  description: string | null;
  coverUrl: string | null;
  publicationYear: number | null;
  category: Category;
  availableCopies: number;
  totalCopies: number;
  available: boolean;
  averageRating: number | null;
  reviews: BookReview[];
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

export interface BookAdmin {
  id: number;
  isbn: string;
  title: string;
  author: string;
  totalCopies: number;
  availableCopies: number;
}

export interface BookRequest {
  title: string;
  author: string;
  isbn: string;
  categoryId: number;
  description: string;
  publicationYear: number | null;
  totalCopies: number;
  coverUrl: string;
}
