import { Injectable } from '@angular/core';

const TOKEN_KEY = 'bookhub.token';

/** Conserve le JWT renvoyé par POST /api/auth/login. */
@Injectable({ providedIn: 'root' })
export class TokenStorage {
  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  saveToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
  }

  clear(): void {
    localStorage.removeItem(TOKEN_KEY);
  }
}
