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
  private readonly currentUser = signal<User | null>(this.loadUser());

  readonly user = this.currentUser.asReadonly();
  readonly isLoggedIn = computed(() => this.currentUser() !== null);

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  start(token: string, user: User): void {
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
    this.currentUser.set(user);
  }

  clear(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.currentUser.set(null);
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
    const token = localStorage.getItem(TOKEN_KEY);
    const user = localStorage.getItem(USER_KEY);
    if (!token || !user || isExpired(token)) {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
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
