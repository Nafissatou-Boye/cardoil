import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { PublicStatsService, PublicStats } from '../../core/services/public-stats.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class LoginComponent implements OnInit {

  login = '';
  motDePasse = '';
  erreur = '';
  chargement = false;

  // ✅ Nouveau — statistiques publiques affichées en jauges dans le hero.
  stats: PublicStats | null = null;
  statsChargement = true;

  constructor(
    private http: HttpClient,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private publicStatsService: PublicStatsService
  ) {}

  ngOnInit(): void {
    this.chargerStats();
  }

  private chargerStats(): void {
    this.publicStatsService.getStats().subscribe({
      next: (data) => {
        this.stats = data;
        this.statsChargement = false;
        this.cdr.detectChanges();
      },
      error: () => {
        // Non bloquant — la page de connexion reste utilisable même si les
        // statistiques échouent à charger, elles restent juste en squelette.
        this.statsChargement = false;
        this.cdr.detectChanges();
      },
    });
  }

  formatVolume(v: number): string {
    if (v >= 1_000_000_000) return (v / 1_000_000_000).toFixed(1).replace('.', ',') + ' Md';
    if (v >= 1_000_000) return (v / 1_000_000).toFixed(1).replace('.', ',') + ' M';
    if (v >= 1_000) return Math.round(v / 1_000) + ' K';
    return v.toString();
  }

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

        if (response.doitChangerMotDePasse) {
          this.router.navigate(['/change-password']);
          return;
        }

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
          case 'ADMIN_DEPARTEMENT':
            this.router.navigate(['/departement/dashboard']);
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