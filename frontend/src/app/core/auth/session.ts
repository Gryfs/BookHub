import { computed, Injectable, signal } from '@angular/core';

import { Role, User } from '../../shared/models/user';

const TOKEN_KEY = 'bookhub.token';
const USER_KEY = 'bookhub.user';

const ROLE_LEVEL: Record<Role, number> = {
  USER: 1,
  LIBRARIAN: 2,
  ADMIN: 3,
};

const HOME_BY_ROLE: Record<Role, string> = {
  USER: '/dashboard',
  LIBRARIAN: '/librarian',
  ADMIN: '/admin',
};

@Injectable({ providedIn: 'root' })
export class Session {
  private expired = false;
  private readonly currentUser = signal<User | null>(this.loadUser());

  readonly user = this.currentUser.asReadonly();
  readonly isLoggedIn = computed(() => this.currentUser() !== null);

  getToken(): string | null {
    return sessionStorage.getItem(TOKEN_KEY);
  }

  start(token: string, user: User): void {
    sessionStorage.setItem(TOKEN_KEY, token);
    sessionStorage.setItem(USER_KEY, JSON.stringify(user));
    this.currentUser.set(user);
  }

  updateUser(user: User): void {
    sessionStorage.setItem(USER_KEY, JSON.stringify(user));
    this.currentUser.set(user);
  }

  clear(): void {
    sessionStorage.removeItem(TOKEN_KEY);
    sessionStorage.removeItem(USER_KEY);
    this.currentUser.set(null);
  }

  checkExpiration(): void {
    const token = this.getToken();
    if (token && isExpired(token)) {
      this.clear();
      this.expired = true;
    }
  }

  consumeExpired(): boolean {
    const wasExpired = this.expired;
    this.expired = false;
    return wasExpired;
  }

  hasRole(role: Role): boolean {
    const user = this.currentUser();
    return user !== null && ROLE_LEVEL[user.role] >= ROLE_LEVEL[role];
  }

  homeUrl(): string {
    const user = this.currentUser();
    return user ? HOME_BY_ROLE[user.role] : '/books';
  }

  private loadUser(): User | null {
    const token = sessionStorage.getItem(TOKEN_KEY);
    const user = sessionStorage.getItem(USER_KEY);
    if (!token || !user || isExpired(token)) {
      this.expired = !!token && isExpired(token);
      sessionStorage.removeItem(TOKEN_KEY);
      sessionStorage.removeItem(USER_KEY);
      return null;
    }
    return JSON.parse(user) as User;
  }
}

function isExpired(token: string): boolean {
  try {
    const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
    return payload.exp * 1000 < Date.now();
  } catch {
    return true;
  }
}
