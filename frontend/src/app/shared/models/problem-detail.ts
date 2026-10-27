/** Erreur renvoyée par l'API (RFC 9457, voir ApiExceptionHandler côté backend). */
export interface ProblemDetail {
  type: string;
  title: string;
  status: number;
  detail: string;
  instance?: string;
  /** Erreurs de validation par champ (400). */
  errors?: Record<string, string>;
}
