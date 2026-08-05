import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminPromotionService, Promotion } from '../../core/services/admin-promotion.service';
import { AdminStationService } from '../../core/services/admin-station.service';
import { Station } from '../../core/services/station.service';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-promotions',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './promotions.html',
  styleUrl: './promotions.css'
})
export class PromotionsComponent implements OnInit {

  listePromotions: Promotion[] = [];
  listeStations: Station[] = [];
  chargement = true;
  enregistrement = false;

  // ✅ Nouveau — état de l'upload d'image, séparé de enregistrement
  // (l'upload se fait pendant qu'on remplit le wizard, pas à la
  // soumission finale).
  uploadEnCours = false;

  // Wizard
  wizardOuvert = false;
  etapeActuelle = 1;
  totalEtapes = 6;
  modeEdition = false;
  erreur = '';

  // Données du formulaire wizard
  promo: Promotion = this.nouvellePromotion();
  dateDebutInput = '';
  dateFinInput = '';
  stationsSelectionnees: number[] = [];

  constructor(
    private adminPromotionService: AdminPromotionService,
    private adminStationService: AdminStationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.charger();
    this.chargerStations();
  }

  charger(): void {
    this.chargement = true;
    this.adminPromotionService.getAll().subscribe({
      next: (data) => {
        this.listePromotions = data;
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
      next: (data) => { this.listeStations = data; this.cdr.detectChanges(); },
      error: () => {}
    });
  }

  nouvellePromotion(): Promotion {
    return {
      nom: '', description: '', imageUrl: null, type: 'POINTS',
      dateDebut: '', dateFin: '',
      montantMinimum: null, stationIds: [],
      plafondParClient: null, plafondGlobal: null, plafondJournalier: null,
      pointsParTranche: null, montantParTranche: null,
      descriptionCadeau: null, stockCadeaux: null,
      probabiliteGain: null, descriptionLot: null
    };
  }

  // ===== IMAGE =====

  // ✅ Nouveau — upload immédiat au choix du fichier (pas à la soumission
  // du wizard) : promo.imageUrl est mis à jour dès que l'upload réussit,
  // ce qui permet l'aperçu à l'étape 1 et au récapitulatif.
  onFichierSelectionne(event: Event): void {
    const input = event.target as HTMLInputElement;
    const fichier = input.files?.[0];
    if (!fichier) return;

    this.uploadEnCours = true;
    this.erreur = '';

    this.adminPromotionService.uploaderImage(fichier).subscribe({
      next: (res) => {
        this.promo.imageUrl = res.url;
        this.uploadEnCours = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.erreur = err.error?.erreur || "Échec de l'upload de l'image";
        this.uploadEnCours = false;
        this.cdr.detectChanges();
      }
    });

  
    input.value = '';
  }

  retirerImage(): void {
    this.promo.imageUrl = null;
  }


  getImageUrl(url: string | null | undefined): string {
    if (!url) return '';
    const base = environment.apiUrl.replace(/\/api\/?$/, '');
    return base + url;
  }

  // ===== WIZARD =====

  ouvrirWizard(): void {
    this.modeEdition = false;
    this.promo = this.nouvellePromotion();
    this.dateDebutInput = '';
    this.dateFinInput = '';
    this.stationsSelectionnees = [];
    this.etapeActuelle = 1;
    this.erreur = '';
    this.enregistrement = false;
    this.wizardOuvert = true;
  }

  ouvrirEdition(promo: Promotion): void {
    this.modeEdition = true;
    this.promo = { ...promo };
    this.dateDebutInput = this.versInputDate(promo.dateDebut);
    this.dateFinInput = this.versInputDate(promo.dateFin);
    this.stationsSelectionnees = promo.stationsConcernees?.map(s => s.id) || [];
    this.etapeActuelle = 1;
    this.erreur = '';
    this.enregistrement = false;
    this.wizardOuvert = true;
  }

  fermerWizard(): void {
    this.wizardOuvert = false;
  }

  etapeSuivante(): void {
    if (!this.validerEtape()) return;
    if (this.etapeActuelle < this.totalEtapes) {
      this.etapeActuelle++;
      this.erreur = '';
    }
  }

  etapePrecedente(): void {
    if (this.etapeActuelle > 1) {
      this.etapeActuelle--;
      this.erreur = '';
    }
  }

  validerEtape(): boolean {
    this.erreur = '';
    switch (this.etapeActuelle) {
      case 1:
        if (!this.promo.nom.trim()) { this.erreur = 'Le nom est obligatoire'; return false; }
        if (!this.promo.type) { this.erreur = 'Le type est obligatoire'; return false; }
        return true;
      case 2:
        if (!this.dateDebutInput) { this.erreur = 'La date de début est obligatoire'; return false; }
        if (!this.dateFinInput) { this.erreur = 'La date de fin est obligatoire'; return false; }
        const debut = new Date(this.dateDebutInput);
        const fin = new Date(this.dateFinInput);
        if (fin <= debut) { this.erreur = 'La date de fin doit être après la date de début'; return false; }
        const jours = (fin.getTime() - debut.getTime()) / (1000 * 60 * 60 * 24);
        if (jours > 180) { this.erreur = 'La durée ne peut pas dépasser 180 jours'; return false; }
        return true;
      case 5:
        if (this.promo.type === 'POINTS') {
          if (!this.promo.pointsParTranche) { this.erreur = 'Nombre de points obligatoire'; return false; }
          if (!this.promo.montantParTranche) { this.erreur = 'Montant par tranche obligatoire'; return false; }
        }
        if (this.promo.type === 'GIFT') {
          if (!this.promo.descriptionCadeau) { this.erreur = 'Description du cadeau obligatoire'; return false; }
        }
        if (this.promo.type === 'SCRATCH') {
          if (!this.promo.probabiliteGain) { this.erreur = 'Probabilité de gain obligatoire'; return false; }
          if (!this.promo.descriptionLot) { this.erreur = 'Description du lot obligatoire'; return false; }
        }
        return true;
      default:
        return true;
    }
  }

  toggleStation(stationId: number): void {
    const idx = this.stationsSelectionnees.indexOf(stationId);
    if (idx === -1) this.stationsSelectionnees.push(stationId);
    else this.stationsSelectionnees.splice(idx, 1);
  }

  isStationSelectionnee(stationId: number): boolean {
    return this.stationsSelectionnees.includes(stationId);
  }

  enregistrer(): void {
    if (this.enregistrement) return;
    this.enregistrement = true;
    this.erreur = '';

    const payload: Promotion = {
      ...this.promo,
      dateDebut: this.dateDebutInput + ':00',
      dateFin: this.dateFinInput + ':00',
      stationIds: this.stationsSelectionnees
    };

    const obs = this.modeEdition && this.promo.id
      ? this.adminPromotionService.update(this.promo.id, payload)
      : this.adminPromotionService.create(payload);

    obs.subscribe({
      next: () => {
        this.enregistrement = false;
        this.fermerWizard();
        this.charger();
      },
      error: (err) => {
        this.enregistrement = false;
        this.erreur = err.error?.erreur || 'Une erreur est survenue';
        this.cdr.detectChanges();
      }
    });
  }

  changerStatut(promo: Promotion, statut: string): void {
    if (!promo.id) return;
    this.adminPromotionService.changerStatut(promo.id, statut).subscribe({
      next: () => this.charger(),
      error: (err) => alert(err.error?.erreur || 'Transition impossible')
    });
  }

  supprimer(promo: Promotion): void {
    if (!promo.id) return;
    if (!confirm(`Supprimer "${promo.nom}" ?`)) return;
    this.adminPromotionService.delete(promo.id).subscribe({
      next: () => this.charger(),
      error: (err) => alert(err.error?.erreur || 'Impossible de supprimer')
    });
  }

  // ===== HELPERS =====

  versInputDate(dateAffichee: string): string {
    if (!dateAffichee) return '';
    const [datePart, timePart] = dateAffichee.split(' ');
    const [jour, mois, annee] = datePart.split('/');
    return `${annee}-${mois}-${jour}T${timePart}`;
  }

  getProgres(): number { return (this.etapeActuelle / this.totalEtapes) * 100; }

  getEtapeLabel(): string {
    const labels = ['Identité', 'Période', 'Éligibilité', 'Limites', 'Récompenses', 'Récapitulatif'];
    return labels[this.etapeActuelle - 1];
  }

  getStatutClass(statut: string | undefined): string {
    switch (statut) {
      case 'DRAFT': return 'draft';
      case 'ACTIVE': return 'active';
      case 'SUSPENDUE': return 'suspendue';
      case 'EXPIREE': return 'expiree';
      case 'ARCHIVEE': return 'archivee';
      default: return '';
    }
  }

  getStatutLabel(statut: string | undefined): string {
    switch (statut) {
      case 'DRAFT': return 'Brouillon';
      case 'ACTIVE': return 'Active';
      case 'SUSPENDUE': return 'Suspendue';
      case 'EXPIREE': return 'Expirée';
      case 'ARCHIVEE': return 'Archivée';
      default: return statut || '';
    }
  }

  getTypeIcon(type: string): string {
    switch (type) {
      case 'POINTS': return '⭐';
      case 'GIFT': return '🎁';
      case 'SCRATCH': return '🎫';
      default: return '';
    }
  }

  getTypeLabel(type: string): string {
    switch (type) {
      case 'POINTS': return 'Points fidélité';
      case 'GIFT': return 'Cadeau direct';
      case 'SCRATCH': return 'Grattage';
      default: return type;
    }
  }
}