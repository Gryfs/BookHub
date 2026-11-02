import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { LoanDesk } from './loan-desk';

describe('LoanDesk', () => {
  let fixture: ComponentFixture<LoanDesk>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoanDesk],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(LoanDesk);
    fixture.detectChanges();
  });

  it('affiche le formulaire comptoir', () => {
    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('h1')?.textContent).toContain('Emprunt au comptoir');
    expect(element.querySelector('input[type="email"]')).toBeTruthy();
    expect(element.querySelector('input[type="number"]')).toBeTruthy();
  });

  it('refuse la soumission sans saisie', () => {
    const form = fixture.nativeElement.querySelector('form') as HTMLFormElement;
    form.requestSubmit();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.field-error')).toBeTruthy();
  });
});
