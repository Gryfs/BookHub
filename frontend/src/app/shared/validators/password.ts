import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export const PASSWORD_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^a-zA-Z\d]).{12,}$/;

export const PASSWORD_HINT =
  'Au moins 12 caractères, avec une majuscule, une minuscule, un chiffre et un caractère spécial.';

export function fieldsMatch(field: string, confirmField: string): ValidatorFn {
  return (group: AbstractControl): ValidationErrors | null => {
    const value = group.get(field)?.value;
    const confirmValue = group.get(confirmField)?.value;
    return value === confirmValue ? null : { passwordsMismatch: true };
  };
}
