// src/app/admin/services/services.component.ts
//
// Liste + création/édition + changement de statut. Auto-suffisant (pas de
// route séparée pour le formulaire) — le formulaire s'ouvre en overlay dans
// ce même composant. L'affectation aux stations utilise la vraie liste
// (AdminStationService), cases à cocher par nom. Upload d'icône réel via
// AdminUploadController (POST /admin/upload/icone), autonome — pas besoin
// que le service existe déjà.

import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  ServiceCatalogue,
  ServiceCatalogueRequest,
  ServiceCatalogueService,
} from '../../core/services/service-catalogue.service';
import { Station } from '../../core/services/station.service';
import { AdminStationService } from '../../core/services/admin-station.service';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-services',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './services.component.html',
  styleUrl: './services.component.css',
})
export class ServicesComponent implements OnInit {
  services: ServiceCatalogue[] = [];
  chargement = true;
  erreur: string | null = null;

  // Overlay création/édition
  formulaireOuvert = false;
  serviceEnEdition: ServiceCatalogue | null = null;
  form: FormGroup;
  enregistrement = false;
  erreurForm: string | null = null;
  iconeUploadEnCours = false;

  // Overlay affectation stations
  stationsOuvert = false;
  serviceStations: ServiceCatalogue | null = null;
  stations: Station[] = [];
  stationsChargement = false;
  stationIdsSelectionnes = new Set<number>();

  readonly categories = [
    { valeur: 'ENERGIE', label: 'Énergie' },
    { valeur: 'LAVAGE', label: 'Lavage' },
    { valeur: 'ENTRETIEN', label: 'Entretien' },
    { valeur: 'BOUTIQUE', label: 'Boutique' },
    { valeur: 'SERVICES_DIGITAUX', label: 'Services digitaux' },
    { valeur: 'AUTRE', label: 'Autre' },
  ];

  constructor(
    private fb: FormBuilder,
    private serviceCatalogueService: ServiceCatalogueService,
    private stationService: AdminStationService,
    private cdr: ChangeDetectorRef
  ) {
    this.form = this.fb.group({
      code: ['', [Validators.required, Validators.maxLength(20)]],
      nom: ['', Validators.required],
      categorie: ['', Validators.required],
      description: [''],
      prix: [null],
      icone: [''],
      couleurHex: ['#3b82f6'],
      obligatoire: [false],
      ordreTri: [0],
    });
  }

  ngOnInit(): void {
    this.chargerServices();
  }

  chargerServices(): void {
    this.chargement = true;
    this.erreur = null;
    this.serviceCatalogueService.getServices().subscribe({
      next: (data) => {
        this.services = data.sort((a, b) => a.ordreTri - b.ordreTri);
        this.chargement = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.erreur = err?.error?.erreur || 'Impossible de charger les services';
        this.chargement = false;
        this.cdr.detectChanges();
      },
    });
  }

  categorieLabel(valeur: string): string {
    return this.categories.find((c) => c.valeur === valeur)?.label || valeur;
  }

  // ✅ Nouveau — le backend renvoie un chemin relatif (/uploads/services/...),
  // chaque client préfixe avec sa propre base. environment.apiUrl inclut
  // déjà /api en suffixe (vu dans station.service.ts), on le retire pour
  // obtenir juste l'hôte.
  iconeUrl(icone: string | null | undefined): string | null {
    if (!icone) return null;
    if (icone.startsWith('http')) return icone;
    const baseHost = environment.apiUrl.replace(/\/api\/?$/, '');
    return baseHost + icone;
  }

  onFichierIconeSelectionne(event: Event): void {
    const input = event.target as HTMLInputElement;
    const fichier = input.files?.[0];
    if (!fichier) return;

    this.iconeUploadEnCours = true;
    this.serviceCatalogueService.uploaderIcone(fichier).subscribe({
      next: (res) => {
        this.form.patchValue({ icone: res.url });
        this.iconeUploadEnCours = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.erreurForm = err?.error?.erreur || 'Erreur lors de l\'envoi de l\'image';
        this.iconeUploadEnCours = false;
        this.cdr.detectChanges();
      },
    });

    // Autorise de re-sélectionner le même fichier après une erreur, sans
    // quoi (change) ne se redéclenche pas pour un fichier identique.
    input.value = '';
  }

  statutClass(statut: string): string {
    switch (statut) {
      case 'ACTIF': return 'badge badge-actif';
      case 'INACTIF': return 'badge badge-inactif';
      case 'ARCHIVE': return 'badge badge-archive';
      default: return 'badge badge-brouillon';
    }
  }

  statutLabel(statut: string): string {
    switch (statut) {
      case 'ACTIF': return 'Actif';
      case 'INACTIF': return 'Inactif';
      case 'ARCHIVE': return 'Archivé';
      default: return 'Brouillon';
    }
  }

  // ── Création / édition ────────────────────────────────────────────────

  ouvrirCreation(): void {
    this.serviceEnEdition = null;
    this.erreurForm = null;
    this.form.reset({
      code: '', nom: '', categorie: '', description: '',
      prix: null, icone: '', couleurHex: '#3b82f6',
      obligatoire: false, ordreTri: 0,
    });
    this.form.get('code')?.enable();
    this.formulaireOuvert = true;
  }

  ouvrirEdition(service: ServiceCatalogue): void {
    this.serviceEnEdition = service;
    this.erreurForm = null;
    this.form.setValue({
      code: service.code,
      nom: service.nom,
      categorie: service.categorie,
      description: service.description || '',
      prix: service.prix,
      icone: service.icone || '',
      couleurHex: service.couleurHex || '#3b82f6',
      obligatoire: service.obligatoire,
      ordreTri: service.ordreTri,
    });
    // code non modifiable après création — cohérent avec le backend, qui
    // l'ignore silencieusement à la modification (jamais réassigné côté
    // service métier).
    this.form.get('code')?.disable();
    this.formulaireOuvert = true;
  }

  fermerFormulaire(): void {
    this.formulaireOuvert = false;
    this.serviceEnEdition = null;
  }

  enregistrer(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.enregistrement = true;
    this.erreurForm = null;

    const valeurs = this.form.getRawValue();
    const request: ServiceCatalogueRequest = {
      code: valeurs.code,
      nom: valeurs.nom,
      categorie: valeurs.categorie,
      description: valeurs.description || undefined,
      prix: valeurs.prix ?? undefined,
      icone: valeurs.icone || undefined,
      couleurHex: valeurs.couleurHex || undefined,
      obligatoire: valeurs.obligatoire,
      ordreTri: valeurs.ordreTri,
    };

    const appel$ = this.serviceEnEdition
      ? this.serviceCatalogueService.modifierService(this.serviceEnEdition.id, request)
      : this.serviceCatalogueService.creerService(request);

    appel$.subscribe({
      next: () => {
        this.enregistrement = false;
        this.fermerFormulaire();
        this.chargerServices();
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.enregistrement = false;
        this.erreurForm = err?.error?.erreur || 'Erreur lors de l\'enregistrement';
        this.cdr.detectChanges();
      },
    });
  }

  // ── Statut ───────────────────────────────────────────────────────────

  changerStatut(service: ServiceCatalogue, statut: string): void {
    this.serviceCatalogueService.changerStatut(service.id, statut).subscribe({
      next: () => {
        this.chargerServices();
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.erreur = err?.error?.erreur || 'Erreur lors du changement de statut';
        this.cdr.detectChanges();
      },
    });
  }

  // ── Affectation stations ────────────────────────────────────────────

  ouvrirStations(service: ServiceCatalogue): void {
    this.serviceStations = service;
    this.stationIdsSelectionnes = new Set(service.stationIds);
    this.stationsOuvert = true;

    // Chargé une seule fois, réutilisé pour les ouvertures suivantes de
    // cette session — évite un appel réseau à chaque clic.
    if (this.stations.length === 0) {
      this.stationsChargement = true;
      this.stationService.getAll().subscribe({
        next: (data) => {
          this.stations = data;
          this.stationsChargement = false;
          this.cdr.detectChanges();
        },
        error: () => {
          this.stationsChargement = false;
          this.cdr.detectChanges();
        },
      });
    }
  }

  fermerStations(): void {
    this.stationsOuvert = false;
    this.serviceStations = null;
  }

  toggleStation(id: number | undefined): void {
    if (id == null) return;
    if (this.stationIdsSelectionnes.has(id)) {
      this.stationIdsSelectionnes.delete(id);
    } else {
      this.stationIdsSelectionnes.add(id);
    }
  }

  enregistrerStations(): void {
    if (!this.serviceStations) return;
    const ids = Array.from(this.stationIdsSelectionnes);

    this.serviceCatalogueService.assignerStations(this.serviceStations.id, ids).subscribe({
      next: () => {
        this.fermerStations();
        this.chargerServices();
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.erreur = err?.error?.erreur || 'Erreur lors de l\'affectation';
        this.cdr.detectChanges();
      },
    });
  }
}