import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminCadeauService, Cadeau } from '../../core/services/admin-cadeau.service';

@Component({
  selector: 'app-cadeaux',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './cadeaux.html',
  styleUrl: './cadeaux.css'
})
export class CadeauxComponent implements OnInit {

  listeCadeaux: Cadeau[] = [];
  chargement = true;
  erreur = '';
  enregistrement = false;

  modalOuvert = false;
  modeEdition = false;
  cadeauCourant: Cadeau = this.nouveauCadeau();

  constructor(
    private adminCadeauService: AdminCadeauService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.chargement = true;
    this.adminCadeauService.getAll().subscribe({
      next: (data) => {
        this.listeCadeaux = data;
        this.chargement = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.chargement = false;
        this.cdr.detectChanges();
      }
    });
  }

  nouveauCadeau(): Cadeau {
    return {
      nom: '',
      type: 'PHYSIQUE',
      coutEnPoints: null,
      stockDisponible: 0,
      image: null,
      descriptionLongue: null,
      dateExpiration: null,
      actif: true
    };
  }

  ouvrirAjout(): void {
    this.modeEdition = false;
    this.cadeauCourant = this.nouveauCadeau();
    this.erreur = '';
    this.enregistrement = false;
    this.modalOuvert = true;
  }

  ouvrirEdition(cadeau: Cadeau): void {
    this.modeEdition = true;
    this.cadeauCourant = { ...cadeau };
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

    const obs = this.modeEdition && this.cadeauCourant.id
      ? this.adminCadeauService.update(this.cadeauCourant.id, this.cadeauCourant)
      : this.adminCadeauService.create(this.cadeauCourant);

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

  toggleActif(cadeau: Cadeau): void {
    if (!cadeau.id) return;
    this.adminCadeauService.toggleActif(cadeau.id).subscribe({
      next: () => this.charger(),
      error: () => {}
    });
  }

  supprimer(cadeau: Cadeau): void {
    if (!cadeau.id) return;
    if (!confirm(`Supprimer "${cadeau.nom}" du catalogue ?`)) return;
    this.adminCadeauService.delete(cadeau.id).subscribe({
      next: () => this.charger(),
      error: (err) => alert(err.error?.erreur || 'Impossible de supprimer')
    });
  }

  getTypeIcon(type: string): string {
    return type === 'PHYSIQUE' ? '📦' : '🎯';
  }

  getTypeLabel(type: string): string {
    return type === 'PHYSIQUE' ? 'Physique' : 'Service';
  }

  formatStock(cadeau: Cadeau): string {
    if (cadeau.stockDisponible === 0) return '∞ Illimité';
    return cadeau.stockDisponible + ' unité(s)';
  }
}