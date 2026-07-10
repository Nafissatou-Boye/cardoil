import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GerantService, GerantDashboard } from '../../core/services/gerant.service';

@Component({
  selector: 'app-gerant-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class DashboardComponent implements OnInit {

  dashboard: GerantDashboard | null = null;
  chargement = true;

  constructor(
    private gerantService: GerantService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.chargement = true;
    this.gerantService.getDashboard().subscribe({
      next: (data) => {
        this.dashboard = data;
        this.chargement = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.chargement = false;
        this.cdr.detectChanges();
      }
    });
  }

  formatMontant(montant: number | null): string {
    if (montant === null || montant === undefined) return '0 FCFA';
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