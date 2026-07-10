import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PaysService, Pays } from '../../core/services/pays.service';

@Component({
  selector: 'app-pays',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './pays.html',
  styleUrl: './pays.css'
})
export class PaysComponent implements OnInit {

  listePays: Pays[] = [];
  chargement = true;
  erreur = '';

  modalOuvert = false;
  modeEdition = false;
  paysCourant: Pays = this.nouveauPays();

  constructor(
    private paysService: PaysService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.chargement = true;
    this.paysService.getAll().subscribe({
      next: (data) => {
        this.listePays = data;
        this.chargement = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.chargement = false;
        this.cdr.detectChanges();
      }
    });
  }

  nouveauPays(): Pays {
    return {
      codeIso: '',
      nom: '',
      devise: '',
      indicatifTel: '',
      actif: true
    };
  }

  ouvrirAjout(): void {
    this.modeEdition = false;
    this.paysCourant = this.nouveauPays();
    this.erreur = '';
    this.modalOuvert = true;
  }

  ouvrirEdition(pays: Pays): void {
    this.modeEdition = true;
    this.paysCourant = { ...pays };
    this.erreur = '';
    this.modalOuvert = true;
  }

  fermerModal(): void {
    this.modalOuvert = false;
  }

  enregistrer(): void {
    this.erreur = '';

    if (this.modeEdition && this.paysCourant.id) {
      this.paysService.update(this.paysCourant.id, this.paysCourant).subscribe({
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
      this.paysService.create(this.paysCourant).subscribe({
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

  supprimer(pays: Pays): void {
    if (!pays.id) return;

    if (!confirm(`Supprimer le pays "${pays.nom}" ?`)) return;

    this.paysService.delete(pays.id).subscribe({
      next: () => this.charger(),
      error: (err) => {
        alert(err.error?.erreur || 'Impossible de supprimer ce pays');
      }
    });
  }
}