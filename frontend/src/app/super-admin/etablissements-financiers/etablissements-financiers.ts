import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EtablissementFinancierService } from './etablissement-financier.service';
import { LiaisonService } from './liaison.service';
import {
  EtablissementFinancier,
  EtablissementFinancierCreate,
  ApiKeyGeneree,
  TypeEtablissement,
  StatutEtablissement,
  LABELS_TYPE_ETABLISSEMENT
} from './etablissement-financier.model';
import { Liaison, LiaisonRequest, CompagnieOption } from './liaison.model';

@Component({
  selector: 'app-etablissements-financiers',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './etablissements-financiers.html',
  styleUrl: './etablissements-financiers.css'
})
export class EtablissementsFinanciersComponent implements OnInit {

  etablissements: EtablissementFinancier[] = [];
  chargement = false;
  erreur: string | null = null;

  labelsType = LABELS_TYPE_ETABLISSEMENT;
  typesDisponibles: TypeEtablissement[] = [
    'BANQUE', 'FINTECH', 'OPERATEUR_MOBILE_MONEY', 'ETABLISSEMENT_MONNAIE_ELECTRONIQUE'
  ];

  afficherModalCreation = false;
  creationEnCours = false;
  formulaire: EtablissementFinancierCreate = this.formulaireVide();

  cleGeneree: ApiKeyGeneree | null = null;
  cleCopiee = false;

  afficherModalLiaisons = false;
  etablissementSelectionne: EtablissementFinancier | null = null;
  liaisons: Liaison[] = [];
  chargementLiaisons = false;
  erreurLiaisons: string | null = null;

  compagniesDisponibles: CompagnieOption[] = [];

  afficherFormAjoutLiaison = false;
  creationLiaisonEnCours = false;
  formulaireLiaison: LiaisonRequest = this.formulaireLiaisonVide();

  constructor(
    private service: EtablissementFinancierService,
    private liaisonService: LiaisonService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.chargement = true;
    this.erreur = null;
    this.service.lister().subscribe({
      next: (data) => {
        this.etablissements = data;
        this.chargement = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.erreur = "Impossible de charger les établissements financiers.";
        this.chargement = false;
        this.cdr.detectChanges();
      }
    });
  }

  ouvrirModalCreation(): void {
    this.formulaire = this.formulaireVide();
    this.afficherModalCreation = true;
  }

  fermerModalCreation(): void {
    this.afficherModalCreation = false;
  }

  soumettreCreation(): void {
    this.creationEnCours = true;
    this.service.creer(this.formulaire).subscribe({
      next: (resultat) => {
        this.creationEnCours = false;
        this.afficherModalCreation = false;
        this.cleGeneree = resultat;
        this.cleCopiee = false;
        this.charger();
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.creationEnCours = false;
        this.erreur = err?.error?.message || "Erreur lors de la création de l'établissement.";
        this.cdr.detectChanges();
      }
    });
  }

  regenererCle(etablissement: EtablissementFinancier): void {
    const confirmation = confirm(
      `Régénérer la clé API de "${etablissement.nom}" ? L'ancienne clé cessera immédiatement de fonctionner.`
    );
    if (!confirmation) return;

    this.service.regenererCle(etablissement.id).subscribe({
      next: (resultat) => {
        this.cleGeneree = resultat;
        this.cleCopiee = false;
        this.charger();
        this.cdr.detectChanges();
      },
      error: () => {
        this.erreur = "Erreur lors de la régénération de la clé.";
        this.cdr.detectChanges();
      }
    });
  }

  changerStatut(etablissement: EtablissementFinancier, nouveauStatut: StatutEtablissement): void {
    this.service.changerStatut(etablissement.id, nouveauStatut).subscribe({
      next: () => {
        this.charger();
        this.cdr.detectChanges();
      },
      error: () => {
        this.erreur = "Erreur lors du changement de statut.";
        this.cdr.detectChanges();
      }
    });
  }

  fermerModalCle(): void {
    this.cleGeneree = null;
  }

 copierCle(): void {
  if (!this.cleGeneree) return;
  navigator.clipboard.writeText(this.cleGeneree.apiKey).then(() => {
    this.cleCopiee = true;
    this.cdr.detectChanges();
  }).catch(() => {
    // Le presse-papiers a échoué (permissions navigateur, contexte non sécurisé...)
    // Repli : sélectionne le texte automatiquement pour un Ctrl+C manuel.
    this.selectionnerTexteCle();
  });
}

selectionnerTexteCle(): void {
  const element = document.getElementById('cle-api-texte');
  if (!element) return;
  const range = document.createRange();
  range.selectNodeContents(element);
  const selection = window.getSelection();
  selection?.removeAllRanges();
  selection?.addRange(range);
}

  private formulaireVide(): EtablissementFinancierCreate {
    return {
      nom: '',
      code: '',
      type: 'OPERATEUR_MOBILE_MONEY',
      emailContact: '',
      telephoneContact: '',
      rateLimitParMinute: 60
    };
  }

  ouvrirModalLiaisons(etablissement: EtablissementFinancier): void {
    this.etablissementSelectionne = etablissement;
    this.afficherModalLiaisons = true;
    this.afficherFormAjoutLiaison = false;
    this.chargerLiaisons();
    this.chargerCompagniesDisponibles();
  }

  fermerModalLiaisons(): void {
    this.afficherModalLiaisons = false;
    this.etablissementSelectionne = null;
    this.liaisons = [];
  }

  chargerLiaisons(): void {
    if (!this.etablissementSelectionne) return;
    this.chargementLiaisons = true;
    this.erreurLiaisons = null;
    this.liaisonService.lister(this.etablissementSelectionne.id).subscribe({
      next: (data) => {
        this.liaisons = data;
        this.chargementLiaisons = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.erreurLiaisons = "Impossible de charger les compagnies liées.";
        this.chargementLiaisons = false;
        this.cdr.detectChanges();
      }
    });
  }

  chargerCompagniesDisponibles(): void {
    this.liaisonService.listerCompagniesDisponibles().subscribe({
      next: (data) => {
        this.compagniesDisponibles = data;
        this.cdr.detectChanges();
      },
      error: () => {
        this.erreurLiaisons = "Impossible de charger la liste des compagnies.";
        this.cdr.detectChanges();
      }
    });
  }

  compagniesNonLiees(): CompagnieOption[] {
    return this.compagniesDisponibles.filter(
      c => !this.liaisons.some(l => l.compagnieId === c.id)
    );
  }

  ouvrirFormAjoutLiaison(): void {
    this.formulaireLiaison = this.formulaireLiaisonVide();
    this.afficherFormAjoutLiaison = true;
  }

  annulerAjoutLiaison(): void {
    this.afficherFormAjoutLiaison = false;
  }

  soumettreLiaison(): void {
    if (!this.etablissementSelectionne || !this.formulaireLiaison.compagnieId) return;

    this.creationLiaisonEnCours = true;
    this.liaisonService.creer(this.etablissementSelectionne.id, this.formulaireLiaison).subscribe({
      next: () => {
        this.creationLiaisonEnCours = false;
        this.afficherFormAjoutLiaison = false;
        this.chargerLiaisons();
        this.charger();
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.creationLiaisonEnCours = false;
        this.erreurLiaisons = err?.error?.message || "Erreur lors de l'ajout de la liaison.";
        this.cdr.detectChanges();
      }
    });
  }

  toggleStatutLiaison(liaison: Liaison): void {
    if (!this.etablissementSelectionne) return;
    const nouveauStatut: StatutEtablissement = liaison.statut === 'ACTIF' ? 'SUSPENDU' : 'ACTIF';

    this.liaisonService.changerStatut(this.etablissementSelectionne.id, liaison.id, nouveauStatut).subscribe({
      next: () => {
        this.chargerLiaisons();
        this.cdr.detectChanges();
      },
      error: () => {
        this.erreurLiaisons = "Erreur lors du changement de statut de la liaison.";
        this.cdr.detectChanges();
      }
    });
  }

  supprimerLiaison(liaison: Liaison): void {
    if (!this.etablissementSelectionne) return;
    const confirmation = confirm(
      `Retirer la liaison avec "${liaison.compagnieNom}" ? Ce partenaire ne pourra plus recharger de clients de cette compagnie.`
    );
    if (!confirmation) return;

    this.liaisonService.supprimer(this.etablissementSelectionne.id, liaison.id).subscribe({
      next: () => {
        this.chargerLiaisons();
        this.charger();
        this.cdr.detectChanges();
      },
      error: () => {
        this.erreurLiaisons = "Erreur lors de la suppression de la liaison.";
        this.cdr.detectChanges();
      }
    });
  }

  private formulaireLiaisonVide(): LiaisonRequest {
    return {
      compagnieId: 0,
      montantMinimum: 100,
      montantMaximumParTransaction: 1000000,
      plafondJournalierParClient: 2000000
    };
  }
}