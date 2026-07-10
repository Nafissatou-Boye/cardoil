import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminRapportService, RapportGlobal } from '../../core/services/admin-rapport.service';

@Component({
  selector: 'app-rapports',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './rapports.html',
  styleUrl: './rapports.css'
})
export class RapportsComponent implements OnInit {

  rapport: RapportGlobal | null = null;
  chargement = true;
  periodeActive = '30D';
  periodes = ['7D', '30D', '3M', '12M'];

  constructor(
    private adminRapportService: AdminRapportService,
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
    this.adminRapportService.getRapport(this.periodeActive).subscribe({
      next: (data) => {
        this.rapport = data;
        this.chargement = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.chargement = false;
        this.cdr.detectChanges();
      }
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