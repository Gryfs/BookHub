import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

import { Auth } from '../../auth/auth';
import { Session } from '../../auth/session';

@Component({
  selector: 'app-header',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './header.html',
  styleUrl: './header.css',
})
export class Header {
  private readonly auth = inject(Auth);
  protected readonly session = inject(Session);

  protected readonly menuOpen = signal(false);

  protected readonly displayName = computed(() => {
    const user = this.session.user();
    return user ? `${user.firstName} ${user.lastName.charAt(0)}.` : '';
  });

  protected readonly homeLabel = computed(() => {
    switch (this.session.user()?.role) {
      case 'ADMIN':
        return 'Administration';
      case 'LIBRARIAN':
        return 'Pilotage';
      default:
        return 'Tableau de bord';
    }
  });

  toggleMenu(): void {
    this.menuOpen.update((open) => !open);
  }

  closeMenu(): void {
    this.menuOpen.set(false);
  }

  logout(): void {
    this.closeMenu();
    this.auth.logout();
  }
}
