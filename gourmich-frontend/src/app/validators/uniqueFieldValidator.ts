import { AsyncValidatorFn, AbstractControl, ValidationErrors } from "@angular/forms";
import { catchError, map, Observable, of, switchMap, timer } from "rxjs";

export function uniqueFieldValidator(
    checkFn: (value: string) => Observable<boolean>,
    errorKey: string = 'notUnique',
    debounceTime: number = 500
  ): AsyncValidatorFn {
    return (control: AbstractControl): Observable<ValidationErrors | null> => {
      if (!control.value) return of(null);
  
      return timer(debounceTime).pipe(
        switchMap(() => checkFn(control.value)),
        map((exists: boolean) => (exists ? { [errorKey]: true } : null)),
        catchError(() => of(null))
      );
    };
  }