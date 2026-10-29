import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, input, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { Session } from '../../core/auth/session';
import { ProblemDetail } from '../../shared/models/problem-detail';
import { Profile as ProfileModel } from '../../shared/models/user';
import { ProfileApi } from './profile-api';

const PHONE_PATTERN = /^(\+33 ?|0)[1-9]([ .-]?[0-9]{2}){4}$/;

@Component({
  selector: 'app-profile',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile implements OnInit {
  private readonly profileApi = inject(ProfileApi);
  private readonly session = inject(Session);

  readonly passwordChanged = input<string>();

  protected readonly profile = signal<ProfileModel | null>(null);
  protected readonly successMessage = signal('');
  protected readonly errorMessage = signal('');
  protected readonly saving = signal(false);

  protected readonly profileForm = new FormGroup({
    firstName: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(100)],
    }),
    lastName: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(100)],
    }),
    phone: new FormControl('', {
      nonNullable: true,
      validators: [Validators.pattern(PHONE_PATTERN), Validators.maxLength(20)],
    }),
    displayMode: new FormControl<'name' | 'pseudonym'>('name', { nonNullable: true }),
    pseudonym: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(50)] }),
  });

  ngOnInit(): void {
    this.profileApi.getProfile().subscribe({
      next: (profile) => this.fillForm(profile),
      error: () => this.errorMessage.set('Impossible de charger votre profil.'),
    });
  }

  realName(): string {
    const { firstName, lastName } = this.profileForm.getRawValue();
    return lastName ? `${firstName} ${lastName.charAt(0)}.` : firstName;
  }

  isInvalid(name: string): boolean {
    const control = this.profileForm.get(name);
    return !!control && control.touched && control.invalid;
  }

  onSubmit(): void {
    const { firstName, lastName, phone, displayMode, pseudonym } = this.profileForm.getRawValue();

    if (displayMode === 'pseudonym' && !pseudonym.trim()) {
      this.profileForm.controls.pseudonym.setErrors({ required: true });
    }
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.successMessage.set('');
    this.errorMessage.set('');

    this.profileApi
      .updateProfile({
        firstName,
        lastName,
        phone,
        pseudonym: displayMode === 'pseudonym' ? pseudonym : '',
      })
      .subscribe({
        next: (profile) => {
          this.saving.set(false);
          this.fillForm(profile);
          this.session.updateUser({
            id: profile.id,
            email: profile.email,
            firstName: profile.firstName,
            lastName: profile.lastName,
            role: profile.role,
          });
          this.successMessage.set('Vos informations ont été enregistrées.');
        },
        error: (error: HttpErrorResponse) => {
          this.saving.set(false);
          this.showServerErrors(error);
        },
      });
  }

  private fillForm(profile: ProfileModel): void {
    this.profile.set(profile);
    this.profileForm.setValue({
      firstName: profile.firstName,
      lastName: profile.lastName,
      phone: profile.phone ?? '',
      displayMode: profile.pseudonym ? 'pseudonym' : 'name',
      pseudonym: profile.pseudonym ?? '',
    });
  }

  private showServerErrors(error: HttpErrorResponse): void {
    const problem = error.error as ProblemDetail | null;
    if (error.status === 400 && problem?.errors) {
      for (const [field, message] of Object.entries(problem.errors)) {
        this.profileForm.get(field)?.setErrors({ server: message });
      }
      this.errorMessage.set('Veuillez corriger les erreurs ci-dessous.');
    } else {
      this.errorMessage.set('Le serveur ne répond pas, veuillez réessayer plus tard.');
    }
  }
}
