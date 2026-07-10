import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { GerantService, TransactionItem, ProduitDisponible } from '../../core/services/gerant.service';

@Component({
  selector: 'app-transactions',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './transactions.html',
  styleUrl: './transactions.css'
})
export class TransactionsComponent implements OnInit {

  transactions: TransactionItem[] = [];
  produits: ProduitDisponible[] = [];
  chargement = true;
  enregistrement = false;

  // Formulaire saisie
  typeTransaction = 'ACHAT';
  produitSelectionne: number | null = null;
  montant: number | null = null;
  erreur = '';
  succes = '';

  constructor(
    private gerantService: GerantService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.charger();
    this.chargerProduits();
  }

  charger(): void {
    this.chargement = true;
    this.gerantService.getTransactionsRecentes().subscribe({
      next: (data) => {
        this.transactions = data;
        this.chargement = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.chargement = false;
        this.cdr.detectChanges();
      }
    });
  }

  chargerProduits(): void {
    this.gerantService.getProduitsDisponibles().subscribe({
      next: (data) => {
        this.produits = data;
        this.cdr.detectChanges();
      },
      error: () => {}
    });
  }

  onTypeChange(): void {
    if (this.typeTransaction === 'RECHARGE') {
      this.produitSelectionne = null;
    }
  }

  getProduitSelectionne(): ProduitDisponible | null {
    return this.produits.find(p => p.id === this.produitSelectionne) || null;
  }

  enregistrerTransaction(): void {
    if (this.enregistrement) return;

    this.erreur = '';
    this.succes = '';

    if (!this.montant || this.montant <= 0) {
      this.erreur = 'Le montant est obligatoire et doit être positif';
      return;
    }

    if (this.typeTransaction === 'ACHAT' && !this.produitSelectionne) {
      this.erreur = 'Veuillez sélectionner un produit pour un achat';
      return;
    }

    this.enregistrement = true;

    this.gerantService.creerTransaction({
      type: this.typeTransaction,
      produitId: this.typeTransaction === 'ACHAT' ? this.produitSelectionne : null,
      montant: this.montant
    }).subscribe({
      next: () => {
        this.enregistrement = false;
        this.succes = '✅ Transaction enregistrée avec succès';
        this.montant = null;
        this.produitSelectionne = null;
        this.typeTransaction = 'ACHAT';
        this.charger();
        this.cdr.detectChanges();
        setTimeout(() => { this.succes = ''; this.cdr.detectChanges(); }, 3000);
      },
      error: (err) => {
        this.enregistrement = false;
        this.erreur = err.error?.erreur || 'Une erreur est survenue';
        this.cdr.detectChanges();
      }
    });
  }

  formatMontant(montant: number | null): string {
    if (montant === null || montant === undefined) return '—';
    return new Intl.NumberFormat('fr-FR').format(montant) + ' FCFA';
  }

  getStatutClass(statut: string): string {
    switch (statut) {
      case 'REUSSIE': return 'reussie';
      case 'ECHEC': return 'echec';
      case 'EN_ATTENTE': return 'attente';
      default: return '';
    }
  }
}