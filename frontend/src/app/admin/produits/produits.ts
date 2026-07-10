import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminProduitService, Produit, PrixRequest, PrixProduit } from '../../core/services/admin-produit.service';

@Component({
  selector: 'app-produits',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './produits.html',
  styleUrl: './produits.css'
})
export class ProduitsComponent implements OnInit {

  listeProduits: Produit[] = [];
  chargement = true;
  erreur = '';

  // Modal produit
  modalProduitOuvert = false;
  modeEdition = false;
  produitCourant: Produit = this.nouveauProduit();

  // Modal prix
  modalPrixOuvert = false;
  produitPourPrix: Produit | null = null;
  prixCourant: PrixRequest = { prixTtc: null, prixHtva: null, prixHtt: null, dateDebut: null, dateFin: null };
  erreurPrix = '';

  constructor(
    private adminProduitService: AdminProduitService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.chargement = true;
    this.adminProduitService.getAll().subscribe({
      next: (data) => {
        this.listeProduits = data;
        this.chargement = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.chargement = false;
        this.cdr.detectChanges();
      }
    });
  }

 nouveauProduit(): Produit {
  return {
    nom: '',
    type: 'LIQUIDE',
    description: '',
    obligatoire: false,
    categorie: '',
    unite: '',
    commissionFixe: null,
    commissionPourcentage: null
  };
 }

  // ===== CRUD PRODUIT =====

  ouvrirAjout(): void {
    this.modeEdition = false;
    this.produitCourant = this.nouveauProduit();
    this.erreur = '';
    this.modalProduitOuvert = true;
  }

  ouvrirEdition(produit: Produit): void {
    this.modeEdition = true;
    this.produitCourant = { ...produit };
    this.erreur = '';
    this.modalProduitOuvert = true;
  }

  fermerModalProduit(): void {
    this.modalProduitOuvert = false;
  }

  enregistrerProduit(): void {
    this.erreur = '';

    if (this.modeEdition && this.produitCourant.id) {
      this.adminProduitService.update(this.produitCourant.id, this.produitCourant).subscribe({
        next: () => {
          this.fermerModalProduit();
          this.charger();
        },
        error: (err) => {
          this.erreur = err.error?.erreur || 'Une erreur est survenue';
          this.cdr.detectChanges();
        }
      });
    } else {
      this.adminProduitService.create(this.produitCourant).subscribe({
        next: () => {
          this.fermerModalProduit();
          this.charger();
        },
        error: (err) => {
          this.erreur = err.error?.erreur || 'Une erreur est survenue';
          this.cdr.detectChanges();
        }
      });
    }
  }

  supprimer(produit: Produit): void {
    if (!produit.id) return;
    if (!confirm(`Supprimer le produit "${produit.nom}" ?`)) return;

    this.adminProduitService.delete(produit.id).subscribe({
      next: () => this.charger(),
      error: (err) => {
        alert(err.error?.erreur || 'Impossible de supprimer ce produit');
      }
    });
  }

  // ===== CHANGEMENT DE STATUT =====

  changerStatut(produit: Produit, nouveauStatut: string): void {
    if (!produit.id) return;

    this.adminProduitService.changerStatut(produit.id, nouveauStatut).subscribe({
      next: () => this.charger(),
      error: (err) => {
        alert(err.error?.erreur || 'Impossible de changer le statut');
      }
    });
  }

  // ajouter aux propriétés de la classe, à côté de erreurPrix
programmation: PrixProduit[] = [];

  // ===== GESTION DU PRIX =====

 // remplacer ouvrirPrix() pour charger aussi la programmation à l'ouverture de la modale
ouvrirPrix(produit: Produit): void {
  this.produitPourPrix = produit;
  this.prixCourant = {
    prixTtc: produit.prixTtcActuel ?? null,
    prixHtva: produit.prixHtvaActuel ?? null,
    prixHtt: produit.prixHttActuel ?? null,
    dateDebut: new Date().toISOString().split('T')[0],
    dateFin: null
  };
  this.erreurPrix = '';
  this.programmation = [];
  this.modalPrixOuvert = true;

  if (produit.id) {
    this.adminProduitService.getProgrammationPrix(produit.id).subscribe({
      next: (data) => {
        this.programmation = data;
        this.cdr.detectChanges();
      }
    });
  }
}

  fermerModalPrix(): void {
    this.modalPrixOuvert = false;
    this.produitPourPrix = null;
  }

// remplacer enregistrerPrix() pour recharger la programmation après ajout, sans fermer la modale
enregistrerPrix(): void {
  if (!this.produitPourPrix?.id) return;
  this.erreurPrix = '';

  this.adminProduitService.definirPrix(this.produitPourPrix.id, this.prixCourant).subscribe({
    next: () => {
      this.charger();
      if (this.produitPourPrix?.id) {
        this.adminProduitService.getProgrammationPrix(this.produitPourPrix.id).subscribe({
          next: (data) => {
            this.programmation = data;
            this.cdr.detectChanges();
          }
        });
      }
    },
    error: (err) => {
      this.erreurPrix = err.error?.erreur || 'Une erreur est survenue';
      this.cdr.detectChanges();
    }
  });
}

  // ===== HELPERS AFFICHAGE =====

  getStatutClass(statut: string | undefined): string {
    switch (statut) {
      case 'BROUILLON': return 'brouillon';
      case 'ACTIF': return 'actif';
      case 'INACTIF': return 'inactif';
      case 'ARCHIVE': return 'archive';
      default: return '';
    }
  }

  getStatutLabel(statut: string | undefined): string {
    switch (statut) {
      case 'BROUILLON': return 'Brouillon';
      case 'ACTIF': return 'Actif';
      case 'INACTIF': return 'Inactif';
      case 'ARCHIVE': return 'Archivé';
      default: return statut || '';
    }
  }

  getTypeLabel(type: string): string {
    return type === 'LIQUIDE' ? '⛽ Litre' : '🔧 Prestation';
  }

  formatPrix(prix: number | null | undefined): string {
    if (prix === null || prix === undefined) return '—';
    return new Intl.NumberFormat('fr-FR').format(prix) + ' FCFA';
  }
}