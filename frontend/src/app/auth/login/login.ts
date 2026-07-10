import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class LoginComponent {

  login = '';
  motDePasse = '';
  erreur = '';
  chargement = false;

  constructor(
    private http: HttpClient,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  onSubmit(): void {
    this.erreur = '';
    this.chargement = true;

    this.http.post<any>(`${environment.apiUrl}/auth/login`, {
      login: this.login,
      motDePasse: this.motDePasse
    }).subscribe({
      next: (response) => {
        localStorage.setItem('token', response.token);
        localStorage.setItem('role', response.role);
        localStorage.setItem('nom', response.nom);
        localStorage.setItem('prenom', response.prenom);
        localStorage.setItem('doitChangerMotDePasse', String(response.doitChangerMotDePasse));

        this.chargement = false;
        this.cdr.detectChanges();

        // Redirection si changement de mot de passe requis
        if (response.doitChangerMotDePasse) {
          this.router.navigate(['/change-password']);
          return;
        }

        // Redirection selon le rôle
        switch (response.role) {
          case 'SUPER_ADMIN':
            this.router.navigate(['/super-admin/dashboard']);
            break;
          case 'ADMIN_COMPAGNIE':
            this.router.navigate(['/admin/dashboard']);
            break;
          case 'GERANT':
            this.router.navigate(['/gerant/dashboard']);
            break;
          case 'ADMIN_ENTREPRISE':
            this.router.navigate(['/entreprise/dashboard']);
            break;
          default:
            this.router.navigate(['/login']);
        }
      },
      error: (err) => {
        this.chargement = false;
        this.erreur = err.error?.message || err.error?.erreur || 'Login ou mot de passe incorrect';
        this.cdr.detectChanges();
      }
    });
  }
}