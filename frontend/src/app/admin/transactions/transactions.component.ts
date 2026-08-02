// src/app/admin/transactions/transactions.component.ts

import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminTransactionService, TransactionLigne } from '../../core/services/admin-transaction.service';
import { AdminStationService } from '../../core/services/admin-station.service';
import { Station } from '../../core/services/station.service';

@Component({
  selector: 'app-transactions',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './transactions.component.html',
  styleUrl: './transactions.component.css',
})
export class TransactionsComponent implements OnInit {
  transactions: TransactionLigne[] = [];
  chargement = true;
  erreur: string | null = null;

  periodeActive = '30D';
  readonly periodes = ['7D', '30D', '3M', '12M'];

  stations: Station[] = [];
  // null = toutes les stations
  stationIdActive: number | null = null;

  constructor(
    private adminTransactionService: AdminTransactionService,
    private adminStationService: AdminStationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.chargerStations();
    this.charger();
  }

  private chargerStations(): void {
    this.adminStationService.getAll().subscribe({
      next: (data) => {
        this.stations = data;
        this.cdr.detectChanges();
      },
      error: () => {
        // Non bloquant — le filtre station reste juste vide si ça échoue,
        // la liste de transactions elle-même charge indépendamment.
      },
    });
  }

  changerPeriode(periode: string): void {
    this.periodeActive = periode;
    this.charger();
  }

  changerStation(stationId: string): void {
    this.stationIdActive = stationId === 'toutes' ? null : Number(stationId);
    this.charger();
  }

  charger(): void {
    this.chargement = true;
    this.erreur = null;
    this.adminTransactionService.getTransactions(this.periodeActive, this.stationIdActive).subscribe({
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