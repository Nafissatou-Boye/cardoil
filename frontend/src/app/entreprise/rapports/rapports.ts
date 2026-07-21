import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RapportService, RapportDepartement, RapportEmploye, SuiviBudget } from '../../core/services/rapport.service';
import { Recharge } from '../../core/services/carte.service';

type Onglet = 'BUDGET' | 'DEPARTEMENTS' | 'EMPLOYES' | 'HISTORIQUE';

@Component({
  selector: 'app-rapports',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './rapports.html',
  styleUrl: './rapports.css'
})
export class RapportsComponent implements OnInit {

  ongletActif: Onglet = 'BUDGET';
  chargement = true;

  suiviBudget: SuiviBudget | null = null;
  rapportDepartements: RapportDepartement[] = [];
  rapportEmployes: RapportEmploye[] = [];
  historiqueGlobal: Recharge[] = [];

  constructor(
    private rapportService: RapportService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.chargerTout();
  }

  chargerTout(): void {
    this.chargement = true;

    this.rapportService.getSuiviBudget().subscribe({
      next: (data) => {
        this.suiviBudget = data;
        this.cdr.detectChanges();
      },
      error: () => {}
    });

    this.rapportService.getRapportDepartements().subscribe({
      next: (data) => {
        this.rapportDepartements = data;
        this.cdr.detectChanges();
      },
      error: () => {}
    });

    this.rapportService.getRapportEmployes().subscribe({
      next: (data) => {
        this.rapportEmployes = data;
        this.chargement = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.chargement = false;
        this.cdr.detectChanges();
      }
    });

    this.rapportService.getHistoriqueGlobal().subscribe({
      next: (data) => {
        this.historiqueGlobal = data;
        this.cdr.detectChanges();
      },
      error: () => {}
    });
  }

  changerOnglet(onglet: Onglet): void {
    this.ongletActif = onglet;
  }

  labelTypeRecharge(type: string): string {
    if (type === 'MANUELLE') return '💰 Recharge manuelle';
    if (type === 'GROUPEE') return '📁 Recharge groupée';
    return '🔄 Dotation automatique';
  }

  get tauxAllocation(): number {
    if (!this.suiviBudget) return 0;
    const total = this.suiviBudget.soldeDisponibleEntreprise + this.suiviBudget.totalBudgetAlloueDepartements;
    if (total === 0) return 0;
    return Math.round((this.suiviBudget.totalBudgetAlloueDepartements / total) * 100);
  }
}