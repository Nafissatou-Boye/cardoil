import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminEntrepriseService, Entreprise, AdminEntrepriseInfo, AdminEntrepriseRequest } from '../../core/services/admin-entreprise.service';

@Component({
  selector: 'app-entreprises',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './entreprises.html',
  styleUrl: './entreprises.css'
})
export class EntreprisesComponent implements OnInit {

  listeEntreprises: Entreprise[] = [];
  chargement = true;
  erreur = '';
  enregistrement = false;

  // Modal entreprise
  modalOuvert = false;
  modeEdition = false;
  entrepriseCourante: Entreprise = this.nouvelleEntreprise();

  // Modal admin
  modalAdminOuvert = false;
  entreprisePourAdmin: Entreprise | null = null;
  adminExistant: AdminEntrepriseInfo | null = null;
  formAdmin: AdminEntrepriseRequest = { nom: '', prenom: '', email: '' };
  erreurAdmin = '';
  enregistrementAdmin = false;
  chargementAdmin = false;

  constructor(
    private adminEntrepriseService: AdminEntrepriseService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.chargement = true;
    this.adminEntrepriseService.getAll().subscribe({
      next: (data) => {
        this.listeEntreprises = data;
        this.chargement = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.chargement = false;
        this.cdr.detectChanges();
      }
    });
  }

  nouvelleEntreprise(): Entreprise {
    return {
      nom: '', code: '', secteurActivite: null,
      adresse: null, telephone: null, email: null, actif: true
    };
  }

  ouvrirAjout(): void {
    this.modeEdition = false;
    this.entrepriseCourante = this.nouvelleEntreprise();
    this.erreur = '';
    this.enregistrement = false;
    this.modalOuvert = true;
  }

  ouvrirEdition(entreprise: Entreprise): void {
    this.modeEdition = true;
    this.entrepriseCourante = { ...entreprise };
    this.erreur = '';
    this.enregistrement = false;
    this.modalOuvert = true;
  }

  fermerModal(): void {
    this.modalOuvert = false;
  }

  enregistrer(): void {
    if (this.enregistrement) return;
    this.enregistrement = true;
    this.erreur = '';

    const obs = this.modeEdition && this.entrepriseCourante.id
      ? this.adminEntrepriseService.update(this.entrepriseCourante.id, this.entrepriseCourante)
      : this.adminEntrepriseService.create(this.entrepriseCourante);

    obs.subscribe({
      next: () => {
        this.enregistrement = false;
        this.fermerModal();
        this.charger();
      },
      error: (err) => {
        this.enregistrement = false;
        this.erreur = err.error?.erreur || 'Une erreur est survenue';
        this.cdr.detectChanges();
      }
    });
  }

  toggleActif(entreprise: Entreprise): void {
    if (!entreprise.id) return;
    this.adminEntrepriseService.toggleActif(entreprise.id).subscribe({
      next: () => this.charger(),
      error: () => {}
    });
  }

  supprimer(entreprise: Entreprise): void {
    if (!entreprise.id) return;
    if (!confirm(`Supprimer l'entreprise "${entreprise.nom}" ?`)) return;
    this.adminEntrepriseService.delete(entreprise.id).subscribe({
      next: () => this.charger(),
      error: (err) => alert(err.error?.erreur || 'Impossible de supprimer')
    });
  }

  // ===== ADMIN ENTREPRISE =====

  ouvrirAdmin(entreprise: Entreprise): void {
    this.entreprisePourAdmin = entreprise;
    this.adminExistant = null;
    this.formAdmin = { nom: '', prenom: '', email: '' };
    this.erreurAdmin = '';
    this.enregistrementAdmin = false;
    this.chargementAdmin = true;
    this.modalAdminOuvert = true;

    this.adminEntrepriseService.getAdmin(entreprise.id!).subscribe({
      next: (admin) => {
        this.adminExistant = admin;
        this.chargementAdmin = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.chargementAdmin = false;
        this.cdr.detectChanges();
      }
    });
  }

  fermerModalAdmin(): void {
    this.modalAdminOuvert = false;
    this.entreprisePourAdmin = null;
  }

  creerAdmin(): void {
    if (this.enregistrementAdmin || !this.entreprisePourAdmin?.id) return;
    this.enregistrementAdmin = true;
    this.erreurAdmin = '';

    this.adminEntrepriseService.createAdmin(this.entreprisePourAdmin.id, this.formAdmin).subscribe({
      next: (data) => {
        this.enregistrementAdmin = false;
        this.adminExistant = data;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.enregistrementAdmin = false;
        this.erreurAdmin = err.error?.erreur || 'Une erreur est survenue';
        this.cdr.detectChanges();
      }
    });
  }

  modeRemplacement = false;

basculerRemplacement(): void {
  this.modeRemplacement = !this.modeRemplacement;
  this.formAdmin = { nom: '', prenom: '', email: '' };
  this.erreurAdmin = '';
}

remplacerAdmin(): void {
  if (this.enregistrementAdmin || !this.entreprisePourAdmin?.id) return;
  this.enregistrementAdmin = true;
  this.erreurAdmin = '';

  this.adminEntrepriseService.remplacerAdmin(this.entreprisePourAdmin.id, this.formAdmin).subscribe({
    next: (data) => {
      this.enregistrementAdmin = false;
      this.modeRemplacement = false;
      this.adminExistant = data;
      this.cdr.detectChanges();
    },
    error: (err) => {
      this.enregistrementAdmin = false;
      this.erreurAdmin = err.error?.erreur || 'Une erreur est survenue';
      this.cdr.detectChanges();
    }
  });
}
}