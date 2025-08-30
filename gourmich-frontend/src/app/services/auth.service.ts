import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { map, Observable } from 'rxjs';
import { environment } from '../../environments/environment.prod';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  
  private baseApiUrl = environment.apiUrl + 'auth/';

  constructor(private http : HttpClient, private router: Router) { }

  login(username: string, password: string) : Observable<any> {
    return this.http.post(`${this.baseApiUrl}login`, { username, password });
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem('token') }
  
  register(user: {username: string; email: string; password: string}): Observable<any> {
    return this.http.post(`${this.baseApiUrl}register`, user);
  }

  saveToken(token: string) : void{
    localStorage.setItem('token', token);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  saveUserName(username: string) : void {
    localStorage.setItem('username', username);
  }

  getUsername(): string | null {
    return localStorage.getItem('username');
  }

  checkEmail(email: string): Observable<boolean> {
    return this.http.get<{ exists: boolean }>(`${this.baseApiUrl}check-email?email=${email}`)
      .pipe(map(response => response.exists));
  }

  
  checkUsername(username: string): Observable<boolean> {
    return this.http.get<{ exists: boolean }>(`${this.baseApiUrl}check-username?username=${username}`)
      .pipe(map(response => response.exists));
  }
}
