import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DepartementService, Departement, AdminDepartementInfo, AdminDepartementRequest, EntrepriseInfo } from '../../core/services/departement.service';

@Component({
  selector: 'app-departements',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './departements.html',
  styleUrl: './departements.css'
})
export class DepartementsComponent implements OnInit {

  listeDepartements: Departement[] = [];
  chargement = true;
  erreur = '';
  enregistrement = false;

  modalOuvert = false;
  modeEdition = false;
  departementCourant: Departement = this.nouveauDepartement();

  // Modal admin département
  modalAdminOuvert = false;
  departementPourAdmin: Departement | null = null;
  adminExistant: AdminDepartementInfo | null = null;
  formAdmin: AdminDepartementRequest = { nom: '', prenom: '', email: '' };
  erreurAdmin = '';
  enregistrementAdmin = false;
  chargementAdmin = false;
  modeRemplacement = false;

  infoEntreprise: EntrepriseInfo | null = null;

  // ===== Créditer budget =====
  modalCrediterOuvert = false;
  departementPourCrediter: Departement | null = null;
  montantCredit: number = 0;
  erreurCredit = '';
  enregistrementCredit = false;

  constructor(
    private departementService: DepartementService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.charger();
    this.chargerInfoEntreprise();
  }

  chargerInfoEntreprise(): void {
    this.departementService.getInfoEntreprise().subscribe({
      next: (data) => {
        this.infoEntreprise = data;
        this.cdr.detectChanges();
      },
      error: () => {}
    });
  }

  charger(): void {
    this.chargement = true;
    this.departementService.getAll().subscribe({
      next: (data) => {
        this.listeDepartements = data;
        this.chargement = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.chargement = false;
        this.cdr.detectChanges();
      }
    });
  }

  nouveauDepartement(): Departement {
    return { nom: '', description: null, budget: 0, actif: true };
  }

  ouvrirAjout(): void {
    this.modeEdition = false;
    this.departementCourant = this.nouveauDepartement();
    this.erreur = '';
    this.enregistrement = false;
    this.modalOuvert = true;
  }

  ouvrirEdition(departement: Departement): void {
    this.modeEdition = true;
    this.departementCourant = { ...departement };
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

    const obs = this.modeEdition && this.departementCourant.id
      ? this.departementService.update(this.departementCourant.id, this.departementCourant)
      : this.departementService.create(this.departementCourant);

    obs.subscribe({
      next: () => {
        this.enregistrement = false;
        this.fermerModal();
        this.charger();
        this.chargerInfoEntreprise();
      },
      error: (err) => {
        this.enregistrement = false;
        this.erreur = err.error?.erreur || err.error?.message || 'Une erreur est survenue';
        this.cdr.detectChanges();
      }
    });
  }

  toggleActif(departement: Departement): void {
    if (!departement.id) return;
    this.departementService.toggleActif(departement.id).subscribe({
      next: () => this.charger(),
      error: () => {}
    });
  }

  supprimer(departement: Departement): void {
    if (!departement.id) return;
    if (!confirm(`Supprimer le département "${departement.nom}" ?`)) return;
    this.departementService.delete(departement.id).subscribe({
      next: () => {
        this.charger();
        this.chargerInfoEntreprise();
      },
      error: (err) => alert(err.error?.erreur || err.error?.message || 'Impossible de supprimer')
    });
  }

  // ===== ADMIN DÉPARTEMENT =====

  ouvrirAdmin(departement: Departement): void {
    this.departementPourAdmin = departement;
    this.adminExistant = null;
    this.formAdmin = { nom: '', prenom: '', email: '' };
    this.erreurAdmin = '';
    this.enregistrementAdmin = false;
    this.modeRemplacement = false;
    this.chargementAdmin = true;
    this.modalAdminOuvert = true;

    this.departementService.getAdmin(departement.id!).subscribe({
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
    this.departementPourAdmin = null;
  }

  creerAdmin(): void {
    if (this.enregistrementAdmin || !this.departementPourAdmin?.id) return;
    this.enregistrementAdmin = true;
    this.erreurAdmin = '';

    this.departementService.createAdmin(this.departementPourAdmin.id, this.formAdmin).subscribe({
      next: (data) => {
        this.enregistrementAdmin = false;
        this.adminExistant = data;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.enregistrementAdmin = false;
        this.erreurAdmin = err.error?.erreur || err.error?.message || 'Une erreur est survenue';
        this.cdr.detectChanges();
      }
    });
  }

  basculerRemplacement(): void {
    this.modeRemplacement = !this.modeRemplacement;
    this.formAdmin = { nom: '', prenom: '', email: '' };
    this.erreurAdmin = '';
  }

  remplacerAdmin(): void {
    if (this.enregistrementAdmin || !this.departementPourAdmin?.id) return;
    this.enregistrementAdmin = true;
    this.erreurAdmin = '';

    this.departementService.remplacerAdmin(this.departementPourAdmin.id, this.formAdmin).subscribe({
      next: (data) => {
        this.enregistrementAdmin = false;
        this.modeRemplacement = false;
        this.adminExistant = data;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.enregistrementAdmin = false;
        this.erreurAdmin = err.error?.erreur || err.error?.message || 'Une erreur est survenue';
        this.cdr.detectChanges();
      }
    });
  }

  // ===== CRÉDITER BUDGET =====

  ouvrirCrediter(departement: Departement): void {
    this.departementPourCrediter = departement;
    this.montantCredit = 0;
    this.erreurCredit = '';
    this.enregistrementCredit = false;
    this.modalCrediterOuvert = true;
  }

  fermerModalCrediter(): void {
    this.modalCrediterOuvert = false;
    this.departementPourCrediter = null;
  }

  confirmerCredit(): void {
    if (this.enregistrementCredit || !this.departementPourCrediter?.id) return;
    this.enregistrementCredit = true;
    this.erreurCredit = '';

    this.departementService.crediterBudget(this.departementPourCrediter.id, this.montantCredit).subscribe({
      next: () => {
        this.enregistrementCredit = false;
        this.fermerModalCrediter();
        this.charger();
        this.chargerInfoEntreprise();
      },
      error: (err) => {
        this.enregistrementCredit = false;
        this.erreurCredit = err.error?.erreur || err.error?.message || 'Une erreur est survenue';
        this.cdr.detectChanges();
      }
    });
  }
}