import { Component, inject, input } from '@angular/core';

import { Session } from '../../core/auth/session';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.html',
})
export class Dashboard {
  protected readonly session = inject(Session);

  readonly heading = input('Tableau de bord');
}
