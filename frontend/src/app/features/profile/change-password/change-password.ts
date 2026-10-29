import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { ProblemDetail } from '../../../shared/models/problem-detail';
import { fieldsMatch, PASSWORD_HINT, PASSWORD_PATTERN } from '../../../shared/validators/password';
import { ProfileApi } from '../profile-api';

@Component({
  selector: 'app-change-password',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './change-password.html',
  styleUrl: '../../auth/auth-form.css',
})
export class ChangePassword {
  private readonly profileApi = inject(ProfileApi);
  private readonly router = inject(Router);

  protected readonly passwordHint = PASSWORD_HINT;
  protected readonly errorMessage = signal('');
  protected readonly saving = signal(false);

  protected readonly passwordForm = new FormGroup(
    {
      currentPassword: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required],
      }),
      newPassword: new FormControl('', {
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
    },
    { validators: fieldsMatch('newPassword', 'confirmPassword') },
  );

  isInvalid(name: string): boolean {
    const control = this.passwordForm.get(name);
    return !!control && control.touched && control.invalid;
  }

  serverError(name: string): string | null {
    return this.passwordForm.get(name)?.getError('server') ?? null;
  }

  onSubmit(): void {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.errorMessage.set('');

    const { currentPassword, newPassword } = this.passwordForm.getRawValue();
    this.profileApi.changePassword({ currentPassword, newPassword }).subscribe({
      next: () => this.router.navigate(['/profile'], { queryParams: { passwordChanged: 1 } }),
      error: (error: HttpErrorResponse) => {
        this.saving.set(false);
        const problem = error.error as ProblemDetail | null;
        if (error.status === 400 && problem?.errors) {
          for (const [field, message] of Object.entries(problem.errors)) {
            this.passwordForm.get(field)?.setErrors({ server: message });
          }
        } else {
          this.errorMessage.set('Le serveur ne répond pas, veuillez réessayer plus tard.');
        }
      },
    });
  }
}
