export interface BookSummary {
  id: number;
  title: string;
  author: string;
  coverUrl: string | null;
  available: boolean;
}
