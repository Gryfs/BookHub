import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, input, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { Auth } from '../../../core/auth/auth';
import { Session } from '../../../core/auth/session';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: '../auth-form.css',
})
export class Login {
  private readonly auth = inject(Auth);
  private readonly session = inject(Session);
  private readonly router = inject(Router);

  readonly expired = input<string>();
  readonly registered = input<string>();

  protected readonly errorMessage = signal('');
  protected readonly loading = signal(false);

  protected readonly loginForm = new FormGroup({
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email],
    }),
    password: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set('');

    this.auth.login(this.loginForm.getRawValue()).subscribe({
      next: () => this.router.navigateByUrl(this.session.homeUrl()),
      error: (error: HttpErrorResponse) => {
        this.loading.set(false);
        this.errorMessage.set(
          error.status === 401
            ? 'Email ou mot de passe incorrect.'
            : 'Le serveur ne répond pas, veuillez réessayer plus tard.',
        );
      },
    });
  }
}
