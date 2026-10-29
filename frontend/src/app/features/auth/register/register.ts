import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { Auth } from '../../../core/auth/auth';
import { ProblemDetail } from '../../../shared/models/problem-detail';
import { fieldsMatch, PASSWORD_HINT, PASSWORD_PATTERN } from '../../../shared/validators/password';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: '../auth-form.css',
})
export class Register {
  private readonly auth = inject(Auth);
  private readonly router = inject(Router);

  protected readonly errorMessage = signal('');
  protected readonly loading = signal(false);

  protected readonly registerForm = new FormGroup(
    {
      firstName: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.maxLength(100)],
      }),
      lastName: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.maxLength(100)],
      }),
      email: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.email, Validators.maxLength(255)],
      }),
      password: new FormControl('', {
        nonNullable: true,
        validators: [
          Validators.required,
          Validators.pattern(PASSWORD_PATTERN),
          Validators.maxLength(72),
        ],
      }),
      confirmPassword: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required],
      }),
      consent: new FormControl(false, { nonNullable: true, validators: [Validators.requiredTrue] }),
    },
    { validators: fieldsMatch('password', 'confirmPassword') },
  );

  protected readonly passwordHint = PASSWORD_HINT;

  isInvalid(name: string): boolean {
    const control = this.registerForm.get(name);
    return !!control && control.touched && control.invalid;
  }

  onSubmit(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      this.errorMessage.set(
        'Impossible de créer le compte. Veuillez corriger les erreurs ci-dessous.',
      );
      return;
    }

    this.loading.set(true);
    this.errorMessage.set('');

    const { firstName, lastName, email, password, consent } = this.registerForm.getRawValue();
    this.auth.register({ firstName, lastName, email, password, consent }).subscribe({
      next: () => this.router.navigate(['/login'], { queryParams: { registered: 1 } }),
      error: (error: HttpErrorResponse) => {
        this.loading.set(false);
        this.showServerErrors(error);
      },
    });
  }

  private showServerErrors(error: HttpErrorResponse): void {
    const problem = error.error as ProblemDetail | null;

    if (error.status === 409) {
      this.registerForm.controls.email.setErrors({ alreadyUsed: true });
    } else if (error.status === 400 && problem?.errors) {
      for (const [field, message] of Object.entries(problem.errors)) {
        this.registerForm.get(field)?.setErrors({ server: message });
      }
    } else {
      this.errorMessage.set('Le serveur ne répond pas, veuillez réessayer plus tard.');
      return;
    }

    this.errorMessage.set(
      'Impossible de créer le compte. Veuillez corriger les erreurs ci-dessous.',
    );
  }
}
