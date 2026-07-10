import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private apiUrl = `${environment.apiUrl}/auth`;

  constructor(private http: HttpClient) {}

  login(login: string, motDePasse: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, { login, motDePasse });
  }

  logout(): void {
    localStorage.clear();
  }

  changerMotDePasse(ancienMotDePasse: string, nouveauMotDePasse: string): Observable<string> {
    return this.http.post(
      `${this.apiUrl}/change-password`,
      { ancienMotDePasse, nouveauMotDePasse },
      { responseType: 'text' }
    );
  }

  getToken(): string | null { return localStorage.getItem('token'); }
  getRole(): string | null { return localStorage.getItem('role'); }
  getNom(): string | null { return localStorage.getItem('nom'); }
  getPrenom(): string | null { return localStorage.getItem('prenom'); }
  doitChangerMotDePasse(): boolean { return localStorage.getItem('doitChangerMotDePasse') === 'true'; }
}