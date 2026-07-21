import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './change-password.html',
  styleUrl: './change-password.css'
})
export class ChangePasswordComponent {

  ancienMotDePasse = '';
  nouveauMotDePasse = '';
  confirmerMotDePasse = '';
  erreur = '';
  succes = '';
  chargement = false;

  prenom = localStorage.getItem('prenom');
  nom = localStorage.getItem('nom');

  constructor(
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  onSubmit(): void {
    this.erreur = '';
    this.succes = '';

    if (this.nouveauMotDePasse !== this.confirmerMotDePasse) {
      this.erreur = 'Les mots de passe ne correspondent pas';
      return;
    }

    if (this.nouveauMotDePasse.length < 6) {
      this.erreur = 'Le mot de passe doit contenir au moins 6 caractères';
      return;
    }

    this.chargement = true;

    this.authService.changerMotDePasse(this.ancienMotDePasse, this.nouveauMotDePasse).subscribe({
      next: () => {
        this.chargement = false;
        localStorage.setItem('doitChangerMotDePasse', 'false');
        this.succes = 'Mot de passe changé avec succès ! Redirection...';
        this.cdr.detectChanges();

        setTimeout(() => {
          const role = localStorage.getItem('role');
          switch (role) {
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
            case 'ADMIN_DEPARTEMENT':
              this.router.navigate(['/departement/dashboard']);
              break;
            default:
              this.router.navigate(['/login']);
          }
        }, 1500);
      },
      error: (err) => {
        this.chargement = false;
        this.erreur = err.error || 'Une erreur est survenue';
        this.cdr.detectChanges();
      }
    });
  }

  logout(): void {
    localStorage.clear();
    this.router.navigate(['/login']);
  }
}