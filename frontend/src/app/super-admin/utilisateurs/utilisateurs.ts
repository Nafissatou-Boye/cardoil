import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SuperAdminUtilisateurService, AdminCompagnie } from '../../core/services/super-admin-utilisateur.service';
import { CompagnieService, Compagnie } from '../../core/services/compagnie.service';

@Component({
  selector: 'app-utilisateurs',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './utilisateurs.html',
  styleUrl: './utilisateurs.css'
})
export class UtilisateursComponent implements OnInit {

  listeAdmins: AdminCompagnie[] = [];
  listeCompagnies: Compagnie[] = [];
  chargement = true;
  erreur = '';
  enregistrement = false;

  modalOuvert = false;
  modeEdition = false;
  adminCourant: AdminCompagnie = this.nouvelAdmin();

  constructor(
    private utilisateurService: SuperAdminUtilisateurService,
    private compagnieService: CompagnieService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.charger();
    this.chargerCompagnies();
  }

  charger(): void {
    this.chargement = true;
    this.utilisateurService.getAll().subscribe({
      next: (data) => {
        this.listeAdmins = data;
        this.chargement = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.chargement = false;
        this.cdr.detectChanges();
      }
    });
  }

  chargerCompagnies(): void {
    this.compagnieService.getAll().subscribe({
      next: (data) => { this.listeCompagnies = data; this.cdr.detectChanges(); },
      error: () => {}
    });
  }

  nouvelAdmin(): AdminCompagnie {
    return { nom: '', prenom: '', email: '', actif: true, compagnieId: null };
  }

  ouvrirAjout(): void {
    this.modeEdition = false;
    this.adminCourant = this.nouvelAdmin();
    this.erreur = '';
    this.enregistrement = false;
    this.modalOuvert = true;
  }

  ouvrirEdition(admin: AdminCompagnie): void {
    this.modeEdition = true;
    this.adminCourant = { ...admin };
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

    const obs = this.modeEdition && this.adminCourant.id
      ? this.utilisateurService.update(this.adminCourant.id, this.adminCourant)
      : this.utilisateurService.create(this.adminCourant);

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

  toggleActif(admin: AdminCompagnie): void {
    if (!admin.id) return;
    this.utilisateurService.toggleActif(admin.id).subscribe({
      next: () => this.charger(),
      error: () => {}
    });
  }

  supprimer(admin: AdminCompagnie): void {
    if (!admin.id) return;
    if (!confirm(`Supprimer le compte de "${admin.prenom} ${admin.nom}" ?`)) return;
    this.utilisateurService.delete(admin.id).subscribe({
      next: () => this.charger(),
      error: (err) => alert(err.error?.erreur || 'Impossible de supprimer')
    });
  }

  formatDate(date: string | undefined): string {
    if (!date) return '—';
    return new Date(date).toLocaleDateString('fr-FR');
  }
}