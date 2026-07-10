import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SuperAdminParametresService, SuperAdminProfil, SuperAdminProfilRequest } from '../../core/services/super-admin-parametres.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-parametres',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './parametres.html',
  styleUrl: './parametres.css'
})
export class ParametresComponent implements OnInit {

  profil: SuperAdminProfil | null = null;
  chargement = true;

  formProfil: SuperAdminProfilRequest = { nom: '', prenom: '', email: '' };

  erreurProfil = '';
  succesProfil = '';
  enregistrementProfil = false;

  ancienMotDePasse = '';
  nouveauMotDePasse = '';
  confirmerMotDePasse = '';
  erreurMdp = '';
  succesMdp = '';
  enregistrementMdp = false;

  constructor(
    private parametresService: SuperAdminParametresService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.chargement = true;
    this.parametresService.getProfil().subscribe({
      next: (data) => {
        this.profil = data;
        this.formProfil = { nom: data.nom, prenom: data.prenom, email: data.email };
        this.chargement = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.chargement = false;
        this.cdr.detectChanges();
      }
    });
  }

  enregistrerProfil(): void {
    this.erreurProfil = '';
    this.succesProfil = '';
    this.enregistrementProfil = true;

    this.parametresService.updateProfil(this.formProfil).subscribe({
      next: (data) => {
        this.profil = data;
        this.enregistrementProfil = false;
        this.succesProfil = '✅ Profil mis à jour avec succès';
        this.cdr.detectChanges();
        setTimeout(() => { this.succesProfil = ''; this.cdr.detectChanges(); }, 3000);
      },
      error: (err) => {
        this.enregistrementProfil = false;
        this.erreurProfil = err.error?.erreur || 'Une erreur est survenue';
        this.cdr.detectChanges();
      }
    });
  }

  changerMotDePasse(): void {
    this.erreurMdp = '';
    this.succesMdp = '';

    if (this.nouveauMotDePasse !== this.confirmerMotDePasse) {
      this.erreurMdp = 'Les mots de passe ne correspondent pas';
      return;
    }

    if (this.nouveauMotDePasse.length < 6) {
      this.erreurMdp = 'Le mot de passe doit contenir au moins 6 caractères';
      return;
    }

    this.enregistrementMdp = true;

    this.authService.changerMotDePasse(this.ancienMotDePasse, this.nouveauMotDePasse).subscribe({
      next: () => {
        this.enregistrementMdp = false;
        this.succesMdp = '✅ Mot de passe changé avec succès';
        this.ancienMotDePasse = '';
        this.nouveauMotDePasse = '';
        this.confirmerMotDePasse = '';
        this.cdr.detectChanges();
        setTimeout(() => { this.succesMdp = ''; this.cdr.detectChanges(); }, 3000);
      },
      error: (err) => {
        this.enregistrementMdp = false;
        this.erreurMdp = err.error || 'Une erreur est survenue';
        this.cdr.detectChanges();
      }
    });
  }
}