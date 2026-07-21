import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Carte, TypeCarteEmploye, StatutCarte, Recharge } from '../../core/services/carte.service';
import { Employe } from '../../core/services/employe.service';
import { EspaceDepartementService } from '../../core/services/espace-departement.service';

@Component({
  selector: 'app-departement-cartes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './cartes.html',
  styleUrl: './cartes.css'
})
export class CartesComponent implements OnInit {

  listeCartes: Carte[] = [];
  employesSansCarte: Employe[] = [];
  chargement = true;
  erreur = '';
  enregistrement = false;

  modalOuvert = false;
  carteCourante: Carte = this.nouvelleCarte();

  modalRechargeOuvert = false;
  carteARecharger: Carte | null = null;
  montantRecharge: number = 0;
  erreurRecharge = '';
  enregistrementRecharge = false;

  modalHistoriqueOuvert = false;
  historiqueRecharges: Recharge[] = [];
  chargementHistorique = false;

  typesCarteDisponibles: { valeur: TypeCarteEmploye; label: string }[] = [
    { valeur: 'RECHARGEABLE_LIBRE', label: 'Libre (rechargeable à tout moment)' },
    { valeur: 'DOTATION_PLAFONNEE', label: 'Dotation plafonnée (remise à zéro mensuelle)' },
    { valeur: 'DOTATION_AVEC_REPORT', label: 'Dotation avec report (cumulable)' }
  ];

  constructor(
    private espaceDepartementService: EspaceDepartementService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.chargement = true;
    this.espaceDepartementService.getCartes().subscribe({
      next: (cartes) => {
        this.listeCartes = cartes;
        this.chargerEmployesSansCarte();
      },
      error: () => {
        this.chargement = false;
        this.cdr.detectChanges();
      }
    });
  }

  chargerEmployesSansCarte(): void {
    this.espaceDepartementService.getEmployes().subscribe({
      next: (employes) => {
        this.employesSansCarte = employes.filter(e => !e.possedeUneCarte);
        this.chargement = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.chargement = false;
        this.cdr.detectChanges();
      }
    });
  }

  nouvelleCarte(): Carte {
    return {
      employeId: 0, typeCarte: 'RECHARGEABLE_LIBRE', dateExpiration: null,
      montantDotationMensuelle: null, dateRenouvellement: null, plafondCumuleMax: null
    };
  }

  get estDotation(): boolean {
    return this.carteCourante.typeCarte === 'DOTATION_PLAFONNEE' || this.carteCourante.typeCarte === 'DOTATION_AVEC_REPORT';
  }

  get estDotationAvecReport(): boolean {
    return this.carteCourante.typeCarte === 'DOTATION_AVEC_REPORT';
  }

  ouvrirAjout(): void {
    this.carteCourante = this.nouvelleCarte();
    this.erreur = '';
    this.enregistrement = false;
    this.modalOuvert = true;
  }

  fermerModal(): void { this.modalOuvert = false; }

  enregistrer(): void {
    if (this.enregistrement) return;
    this.enregistrement = true;
    this.erreur = '';

    this.espaceDepartementService.createCarte(this.carteCourante).subscribe({
      next: () => { this.enregistrement = false; this.fermerModal(); this.charger(); },
      error: (err) => {
        this.enregistrement = false;
        this.erreur = err.error?.erreur || err.error?.message || 'Une erreur est survenue';
        this.cdr.detectChanges();
      }
    });
  }

  transitionsPossibles(statut: StatutCarte | undefined): StatutCarte[] {
    switch (statut) {
      case 'ACTIVE': return ['SUSPENDUE', 'BLOQUEE', 'EXPIREE'];
      case 'SUSPENDUE': return ['ACTIVE', 'BLOQUEE'];
      case 'BLOQUEE': return ['ACTIVE', 'EXPIREE'];
      default: return [];
    }
  }

  labelStatut(statut: StatutCarte): string {
    const labels: Record<StatutCarte, string> = {
      ACTIVE: '✅ Activer', SUSPENDUE: '⏸️ Suspendre', BLOQUEE: '🚫 Bloquer', EXPIREE: '⌛ Expirer'
    };
    return labels[statut];
  }

  changerStatut(carte: Carte, nouveauStatut: StatutCarte): void {
    if (!carte.id) return;
    this.espaceDepartementService.changerStatutCarte(carte.id, nouveauStatut).subscribe({
      next: () => this.charger(),
      error: (err) => alert(err.error?.erreur || err.error?.message || 'Transition impossible')
    });
  }

  ouvrirRecharge(carte: Carte): void {
    this.carteARecharger = carte;
    this.montantRecharge = 0;
    this.erreurRecharge = '';
    this.enregistrementRecharge = false;
    this.modalRechargeOuvert = true;
  }

  fermerModalRecharge(): void { this.modalRechargeOuvert = false; this.carteARecharger = null; }

  confirmerRecharge(): void {
    if (this.enregistrementRecharge || !this.carteARecharger?.id) return;
    this.enregistrementRecharge = true;
    this.erreurRecharge = '';

    this.espaceDepartementService.rechargerCarte(this.carteARecharger.id, this.montantRecharge).subscribe({
      next: () => { this.enregistrementRecharge = false; this.fermerModalRecharge(); this.charger(); },
      error: (err) => {
        this.enregistrementRecharge = false;
        this.erreurRecharge = err.error?.erreur || err.error?.message || 'Une erreur est survenue';
        this.cdr.detectChanges();
      }
    });
  }

  ouvrirHistorique(carte: Carte): void {
    if (!carte.id) return;
    this.modalHistoriqueOuvert = true;
    this.chargementHistorique = true;
    this.historiqueRecharges = [];

    this.espaceDepartementService.historiqueRecharges(carte.id).subscribe({
      next: (data) => { this.historiqueRecharges = data; this.chargementHistorique = false; this.cdr.detectChanges(); },
      error: () => { this.chargementHistorique = false; this.cdr.detectChanges(); }
    });
  }

  fermerModalHistorique(): void { this.modalHistoriqueOuvert = false; }

  labelTypeCarte(type: TypeCarteEmploye): string {
    return this.typesCarteDisponibles.find(t => t.valeur === type)?.label || type;
  }
}