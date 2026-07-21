import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CarteService, Carte, TypeCarteEmploye, StatutCarte, Recharge, LigneRecharge, RechargeGroupee } from '../../core/services/carte.service';
import { EmployeService, Employe } from '../../core/services/employe.service';

@Component({
  selector: 'app-cartes',
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

  // ===== Recharge groupée =====
  modalRechargeGroupeeOuvert = false;
  nomFichierImporte = '';
  lignesPreview: LigneRecharge[] = [];
  erreurImport = '';
  traitementEnCours = false;
  rapportRecharge: RechargeGroupee | null = null;

  modalHistoriqueGroupeOuvert = false;
  historiqueGroupe: RechargeGroupee[] = [];
  chargementHistoriqueGroupe = false;

  typesCarteDisponibles: { valeur: TypeCarteEmploye; label: string }[] = [
    { valeur: 'RECHARGEABLE_LIBRE', label: 'Libre (rechargeable à tout moment)' },
    { valeur: 'DOTATION_PLAFONNEE', label: 'Dotation plafonnée (remise à zéro mensuelle)' },
    { valeur: 'DOTATION_AVEC_REPORT', label: 'Dotation avec report (cumulable)' }
  ];

  constructor(
    private carteService: CarteService,
    private employeService: EmployeService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.chargement = true;
    this.carteService.getAll().subscribe({
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
    this.employeService.getAll().subscribe({
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
      employeId: 0,
      typeCarte: 'RECHARGEABLE_LIBRE',
      dateExpiration: null,
      montantDotationMensuelle: null,
      dateRenouvellement: null,
      plafondCumuleMax: null
    };
  }

  get estDotation(): boolean {
    return this.carteCourante.typeCarte === 'DOTATION_PLAFONNEE'
        || this.carteCourante.typeCarte === 'DOTATION_AVEC_REPORT';
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

  fermerModal(): void {
    this.modalOuvert = false;
  }

  enregistrer(): void {
    if (this.enregistrement) return;
    this.enregistrement = true;
    this.erreur = '';

    this.carteService.create(this.carteCourante).subscribe({
      next: () => {
        this.enregistrement = false;
        this.fermerModal();
        this.charger();
      },
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
      ACTIVE: '✅ Activer',
      SUSPENDUE: '⏸️ Suspendre',
      BLOQUEE: '🚫 Bloquer',
      EXPIREE: '⌛ Expirer'
    };
    return labels[statut];
  }

  changerStatut(carte: Carte, nouveauStatut: StatutCarte): void {
    if (!carte.id) return;
    this.carteService.changerStatut(carte.id, nouveauStatut).subscribe({
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

  fermerModalRecharge(): void {
    this.modalRechargeOuvert = false;
    this.carteARecharger = null;
  }

  confirmerRecharge(): void {
    if (this.enregistrementRecharge || !this.carteARecharger?.id) return;
    this.enregistrementRecharge = true;
    this.erreurRecharge = '';

    this.carteService.recharger(this.carteARecharger.id, this.montantRecharge).subscribe({
      next: () => {
        this.enregistrementRecharge = false;
        this.fermerModalRecharge();
        this.charger();
      },
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

    this.carteService.historique(carte.id).subscribe({
      next: (data) => {
        this.historiqueRecharges = data;
        this.chargementHistorique = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.chargementHistorique = false;
        this.cdr.detectChanges();
      }
    });
  }

  fermerModalHistorique(): void {
    this.modalHistoriqueOuvert = false;
  }

  renouveler(carte: Carte): void {
    if (!carte.id) return;
    if (!confirm(`Déclencher manuellement le renouvellement mensuel de la carte ${carte.numeroCarte} ?`)) return;
    this.carteService.renouveler(carte.id).subscribe({
      next: () => this.charger(),
      error: (err) => alert(err.error?.erreur || err.error?.message || 'Erreur lors du renouvellement')
    });
  }

  labelTypeCarte(type: TypeCarteEmploye): string {
    return this.typesCarteDisponibles.find(t => t.valeur === type)?.label || type;
  }

  // ===== RECHARGE GROUPÉE =====

  ouvrirRechargeGroupee(): void {
    this.nomFichierImporte = '';
    this.lignesPreview = [];
    this.erreurImport = '';
    this.rapportRecharge = null;
    this.traitementEnCours = false;
    this.modalRechargeGroupeeOuvert = true;
  }

  fermerModalRechargeGroupee(): void {
    this.modalRechargeGroupeeOuvert = false;
  }

  onFichierSelectionne(event: Event): void {
    const input = event.target as HTMLInputElement;
    const fichier = input.files?.[0];
    if (!fichier) return;

    this.nomFichierImporte = fichier.name;
    this.erreurImport = '';
    this.rapportRecharge = null;

    const lecteur = new FileReader();
    lecteur.onload = () => {
      const contenu = lecteur.result as string;
      this.parserCsv(contenu);
      this.cdr.detectChanges();
    };
    lecteur.readAsText(fichier);
  }

  private parserCsv(contenu: string): void {
    const lignes = contenu.split(/\r?\n/).map(l => l.trim()).filter(l => l.length > 0);

    if (lignes.length === 0) {
      this.erreurImport = 'Le fichier est vide';
      this.lignesPreview = [];
      return;
    }

    // Ignore la première ligne si elle ressemble à un en-tête (pas de "CARD-")
    const premiereLigne = lignes[0].toUpperCase();
    const contientEntete = !premiereLigne.includes('CARD-');
    const lignesDonnees = contientEntete ? lignes.slice(1) : lignes;

    const resultat: LigneRecharge[] = [];
    for (const ligne of lignesDonnees) {
      const colonnes = ligne.split(/[,;]/).map(c => c.trim());
      const numeroCarte = colonnes[0] || '';
      const montant = parseFloat(colonnes[1]);
      const commentaire = colonnes[2] || '';

      resultat.push({
        numeroCarte,
        montant: isNaN(montant) ? 0 : montant,
        commentaire
      });
    }

    if (resultat.length === 0) {
      this.erreurImport = 'Aucune ligne exploitable trouvée dans le fichier';
    }

    this.lignesPreview = resultat;
  }

  ligneValide(ligne: LigneRecharge): boolean {
    return !!ligne.numeroCarte && ligne.montant >= 1;
  }

  get nombreLignesValides(): number {
    return this.lignesPreview.filter(l => this.ligneValide(l)).length;
  }

  confirmerRechargeGroupee(): void {
    if (this.traitementEnCours || this.lignesPreview.length === 0) return;
    this.traitementEnCours = true;
    this.erreurImport = '';

    this.carteService.rechargerGroupe(this.nomFichierImporte, this.lignesPreview).subscribe({
      next: (rapport) => {
        this.traitementEnCours = false;
        this.rapportRecharge = rapport;
        this.lignesPreview = [];
        this.charger();
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.traitementEnCours = false;
        this.erreurImport = err.error?.erreur || err.error?.message || 'Une erreur est survenue';
        this.cdr.detectChanges();
      }
    });
  }

  ouvrirHistoriqueGroupe(): void {
    this.modalHistoriqueGroupeOuvert = true;
    this.chargementHistoriqueGroupe = true;
    this.historiqueGroupe = [];

    this.carteService.historiqueGroupe().subscribe({
      next: (data) => {
        this.historiqueGroupe = data;
        this.chargementHistoriqueGroupe = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.chargementHistoriqueGroupe = false;
        this.cdr.detectChanges();
      }
    });
  }

  fermerModalHistoriqueGroupe(): void {
    this.modalHistoriqueGroupeOuvert = false;
  }
}