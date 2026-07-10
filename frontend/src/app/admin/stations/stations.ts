import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminStationService } from '../../core/services/admin-station.service';
import { Station } from '../../core/services/station.service';

@Component({
  selector: 'app-stations',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './stations.html',
  styleUrl: './stations.css'
})
export class StationsComponent implements OnInit {

  listeStations: Station[] = [];
  chargement = true;
  erreur = '';
  enregistrement = false;

  modalOuvert = false;
  modeEdition = false;
  stationCourante: Station = this.nouvelleStation();

  // Géolocalisation
  localisationEnCours = false;
  localisationErreur = '';

  constructor(
    private adminStationService: AdminStationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.chargement = true;
    this.adminStationService.getAll().subscribe({
      next: (data) => {
        this.listeStations = data;
        this.chargement = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.chargement = false;
        this.cdr.detectChanges();
      }
    });
  }

  nouvelleStation(): Station {
    return {
      nom: '',
      adresse: '',
      latitude: null,
      longitude: null,
      telephone: '',
      actif: true
    };
  }

  ouvrirAjout(): void {
    this.modeEdition = false;
    this.stationCourante = this.nouvelleStation();
    this.erreur = '';
    this.enregistrement = false;
    this.localisationErreur = '';
    this.modalOuvert = true;
  }

  ouvrirEdition(station: Station): void {
    this.modeEdition = true;
    this.stationCourante = { ...station };
    this.erreur = '';
    this.enregistrement = false;
    this.localisationErreur = '';
    this.modalOuvert = true;
  }

  fermerModal(): void {
    this.modalOuvert = false;
    this.enregistrement = false;
    this.localisationErreur = '';
  }

  // ===== GÉOLOCALISATION AUTOMATIQUE =====
  localiserAdresse(): void {
    if (!this.stationCourante.adresse || this.stationCourante.adresse.trim() === '') {
      this.localisationErreur = 'Veuillez saisir une adresse avant de localiser';
      return;
    }

    this.localisationEnCours = true;
    this.localisationErreur = '';

    const adresse = encodeURIComponent(this.stationCourante.adresse + ', Sénégal');
    const url = `https://nominatim.openstreetmap.org/search?q=${adresse}&format=json&limit=1`;

    fetch(url, { headers: { 'Accept-Language': 'fr' } })
      .then(res => res.json())
      .then(data => {
        if (data && data.length > 0) {
          this.stationCourante.latitude = parseFloat(parseFloat(data[0].lat).toFixed(6));
          this.stationCourante.longitude = parseFloat(parseFloat(data[0].lon).toFixed(6));
          this.localisationErreur = '';
        } else {
          this.localisationErreur = 'Adresse introuvable — saisissez les coordonnées manuellement';
        }
        this.localisationEnCours = false;
        this.cdr.detectChanges();
      })
      .catch(() => {
        this.localisationErreur = 'Erreur de géolocalisation — saisissez les coordonnées manuellement';
        this.localisationEnCours = false;
        this.cdr.detectChanges();
      });
  }

  enregistrer(): void {
    if (this.enregistrement) return;
    this.enregistrement = true;
    this.erreur = '';

    if (this.modeEdition && this.stationCourante.id) {
      this.adminStationService.update(this.stationCourante.id, this.stationCourante).subscribe({
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
      this.adminStationService.create(this.stationCourante).subscribe({
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

  supprimer(station: Station): void {
    if (!station.id) return;
    if (!confirm(`Supprimer la station "${station.nom}" ?`)) return;

    this.adminStationService.delete(station.id).subscribe({
      next: () => this.charger(),
      error: (err) => {
        alert(err.error?.erreur || 'Impossible de supprimer cette station');
      }
    });
  }
}