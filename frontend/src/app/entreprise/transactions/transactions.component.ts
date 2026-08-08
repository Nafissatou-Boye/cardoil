// src/app/entreprise/transactions/transactions.component.ts
//
// ✅ Nouveau — même pattern que admin/transactions/transactions.component.ts
// (Admin Compagnie), sans le filtre station (pas de sens pour un Admin
// Entreprise, qui ne gère pas de personnel de station).

import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EntrepriseTransactionService, TransactionEmployeLigne } from '../../core/services/entreprise-transaction.service';

@Component({
  selector: 'app-entreprise-transactions',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './transactions.component.html',
  styleUrl: './transactions.component.css',
})
export class TransactionsComponent implements OnInit {
  transactions: TransactionEmployeLigne[] = [];
  chargement = true;
  erreur: string | null = null;

  periodeActive = '30D';
  readonly periodes = ['7D', '30D', '3M', '12M'];

  constructor(
    private entrepriseTransactionService: EntrepriseTransactionService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.charger();
  }

  changerPeriode(periode: string): void {
    this.periodeActive = periode;
    this.charger();
  }

  charger(): void {
    this.chargement = true;
    this.erreur = null;
    this.entrepriseTransactionService.getTransactions(this.periodeActive).subscribe({
      next: (data) => {
        this.transactions = data;
        this.chargement = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.erreur = err?.error?.erreur || 'Impossible de charger les transactions';
        this.chargement = false;
        this.cdr.detectChanges();
      },
    });
  }

  formatMontant(montant: number): string {
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

  getTypeLabel(type: string): string {
    return type === 'ACHAT' ? '🛒 Achat' : '💳 Recharge';
  }
}