import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminPersonnelService, Personnel } from '../../core/services/admin-personnel.service';
import { AdminStationService } from '../../core/services/admin-station.service';
import { Station } from '../../core/services/station.service';

@Component({
  selector: 'app-personnel',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './personnel.html',
  styleUrl: './personnel.css'
})
export class PersonnelComponent implements OnInit {

  listePersonnel: Personnel[] = [];
  listeStations: Station[] = [];
  chargement = true;
  erreur = '';
  enregistrement = false; // ← Protection double-clic

  modalOuvert = false;
  modeEdition = false;
  personnelCourant: Personnel = this.nouveauPersonnel();

  constructor(
    private adminPersonnelService: AdminPersonnelService,
    private adminStationService: AdminStationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.charger();
    this.chargerStations();
  }

  charger(): void {
    this.chargement = true;
    this.adminPersonnelService.getAll().subscribe({
      next: (data) => {
        this.listePersonnel = data;
        this.chargement = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.chargement = false;
        this.cdr.detectChanges();
      }
    });
  }

  chargerStations(): void {
    this.adminStationService.getAll().subscribe({
      next: (data) => {
        this.listeStations = data;
        this.cdr.detectChanges();
      },
      error: () => {}
    });
  }

nouveauPersonnel(): Personnel {
  return {
    login: '',
    nom: '',
    prenom: '',
    email: '',
    actif: true,
    stationId: null,
    role: 'GERANT'
  };
}

  ouvrirAjout(): void {
    this.modeEdition = false;
    this.personnelCourant = this.nouveauPersonnel();
    this.erreur = '';
    this.enregistrement = false;
    this.modalOuvert = true;
  }

  ouvrirEdition(personnel: Personnel): void {
    this.modeEdition = true;
    this.personnelCourant = {
      ...personnel,
      motDePasse: '',
      stationId: personnel.station?.id ?? null
    };
    this.erreur = '';
    this.enregistrement = false;
    this.modalOuvert = true;
  }

  fermerModal(): void {
    this.modalOuvert = false;
    this.enregistrement = false;
  }

  enregistrer(): void {
    if (this.enregistrement) return; // ← Bloque si déjà en cours
    this.enregistrement = true;
    this.erreur = '';

    if (this.modeEdition && this.personnelCourant.id) {
      this.adminPersonnelService.update(this.personnelCourant.id, this.personnelCourant).subscribe({
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
    } else {
      this.adminPersonnelService.create(this.personnelCourant).subscribe({
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
  }

  supprimer(personnel: Personnel): void {
    if (!personnel.id) return;
    if (!confirm(`Supprimer "${personnel.prenom} ${personnel.nom}" ?`)) return;

    this.adminPersonnelService.delete(personnel.id).subscribe({
      next: () => this.charger(),
      error: (err) => {
        alert(err.error?.erreur || 'Impossible de supprimer cet employé');
      }
    });
  }
}