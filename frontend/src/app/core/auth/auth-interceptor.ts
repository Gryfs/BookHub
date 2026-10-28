import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Session } from './session';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const session = inject(Session);
  const router = inject(Router);
  const token = session.getToken();

  if (!token || !req.url.startsWith(environment.apiUrl)) {
    return next(req);
  }

  const authReq = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        session.clear();
        router.navigate(['/login'], { queryParams: { expired: 1 } });
      }
      return throwError(() => error);
    }),
  );
};
