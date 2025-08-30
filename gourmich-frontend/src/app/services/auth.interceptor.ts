import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from './auth.service';
import { Router } from '@angular/router';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.authService.getToken();

    // URLs publiques
    const publicUrls = [
      { url: '/api/', method: 'GET' },
      { url: '/api/recipes/latest', method: 'GET' },
      { url: '/api/recipes', method: 'GET' },
      { url: '/api/auth/register', method: 'POST' },
      { url: '/api/auth/login', method: 'POST' },
      { url: '/api/auth/check-email', method: 'GET' },
      { url: '/api/auth/check-username', method: 'GET' }
    ];

    
    const urlWithoutParams = req.url.split('?')[0];

    
    const isPublic = publicUrls.some(entry => {
      return (
        (urlWithoutParams === entry.url || urlWithoutParams.startsWith(entry.url)) &&
        req.method === entry.method
      );
    });

    
    let authReq = req;
    if (token && !isPublic) {
      authReq = req.clone({
        setHeaders: { Authorization: `Bearer ${token}` }
      });
    }

    

    return next.handle(authReq).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401 && !isPublic) {
          
          this.authService.logout();
          this.router.navigate(['/login']);
        }
        return throwError(() => error);
      })
    );
  }
}