import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CompagnieService, Compagnie } from '../../core/services/compagnie.service';
import { PaysService, Pays } from '../../core/services/pays.service';

@Component({
  selector: 'app-compagnies',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './compagnies.html',
  styleUrl: './compagnies.css'
})
export class CompagniesComponent implements OnInit {

  listeCompagnies: Compagnie[] = [];
  listePays: Pays[] = [];
  chargement = true;
  erreur = '';

  modalOuvert = false;
  modeEdition = false;
  compagnieCourante: Compagnie = this.nouvelleCompagnie();

  constructor(
    private compagnieService: CompagnieService,
    private paysService: PaysService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.charger();
    this.chargerPays();
  }

  charger(): void {
    this.chargement = true;
    this.compagnieService.getAll().subscribe({
      next: (data) => {
        this.listeCompagnies = data;
        this.chargement = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.chargement = false;
        this.cdr.detectChanges();
      }
    });
  }

  chargerPays(): void {
    this.paysService.getAll().subscribe({
      next: (data) => {
        this.listePays = data;
        this.cdr.detectChanges();
      },
      error: () => {}
    });
  }

  nouvelleCompagnie(): Compagnie {
    return {
      nom: '',
      code: '',
      adresse: '',
      telephone: '',
      email: '',
      actif: true,
      paysId: undefined
    };
  }

  ouvrirAjout(): void {
    this.modeEdition = false;
    this.compagnieCourante = this.nouvelleCompagnie();
    if (this.listePays.length > 0) {
      this.compagnieCourante.paysId = this.listePays[0].id;
    }
    this.erreur = '';
    this.modalOuvert = true;
  }

  ouvrirEdition(compagnie: Compagnie): void {
    this.modeEdition = true;
    this.compagnieCourante = {
      ...compagnie,
      paysId: compagnie.pays?.id
    };
    this.erreur = '';
    this.modalOuvert = true;
  }

  fermerModal(): void {
    this.modalOuvert = false;
  }

  enregistrer(): void {
    this.erreur = '';

    if (this.modeEdition && this.compagnieCourante.id) {
      this.compagnieService.update(this.compagnieCourante.id, this.compagnieCourante).subscribe({
        next: () => {
          this.fermerModal();
          this.charger();
        },
        error: (err) => {
          this.erreur = err.error?.erreur || 'Une erreur est survenue';
          this.cdr.detectChanges();
        }
      });
    } else {
      this.compagnieService.create(this.compagnieCourante).subscribe({
        next: () => {
          this.fermerModal();
          this.charger();
        },
        error: (err) => {
          this.erreur = err.error?.erreur || 'Une erreur est survenue';
          this.cdr.detectChanges();
        }
      });
    }
  }

  supprimer(compagnie: Compagnie): void {
    if (!compagnie.id) return;

    if (!confirm(`Supprimer la compagnie "${compagnie.nom}" ?`)) return;

    this.compagnieService.delete(compagnie.id).subscribe({
      next: () => this.charger(),
      error: (err) => {
        alert(err.error?.erreur || 'Impossible de supprimer cette compagnie');
      }
    });
  }
}