import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EmployeService } from '../../core/services/employe.service';
import { DepartementService } from '../../core/services/departement.service';
import { CarteService, Carte } from '../../core/services/carte.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard implements OnInit {

  chargement = true;

  nombreEmployes = 0;
  nombreDepartements = 0;
  soldeTotal = 0;
  soldeDisponibleEntreprise = 0;

  cartesActives = 0;
  cartesSuspendues = 0;
  cartesBloquees = 0;
  cartesExpirees = 0;

  cartesFaibles: Carte[] = [];

  constructor(
    private employeService: EmployeService,
    private departementService: DepartementService,
    private carteService: CarteService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.chargement = true;

    this.employeService.getAll().subscribe({
      next: (employes) => {
        this.nombreEmployes = employes.length;
        this.cdr.detectChanges();
      },
      error: () => {}
    });

    this.departementService.getAll().subscribe({
      next: (departements) => {
        this.nombreDepartements = departements.length;
        this.cdr.detectChanges();
      },
      error: () => {}
    });

    this.departementService.getInfoEntreprise().subscribe({
      next: (info) => {
        this.soldeDisponibleEntreprise = info.soldeDisponible;
        this.cdr.detectChanges();
      },
      error: () => {}
    });

    this.carteService.getAll().subscribe({
      next: (cartes) => {
        this.cartesActives = cartes.filter(c => c.statut === 'ACTIVE').length;
        this.cartesSuspendues = cartes.filter(c => c.statut === 'SUSPENDUE').length;
        this.cartesBloquees = cartes.filter(c => c.statut === 'BLOQUEE').length;
        this.cartesExpirees = cartes.filter(c => c.statut === 'EXPIREE').length;

        this.soldeTotal = cartes
          .filter(c => c.statut === 'ACTIVE')
          .reduce((total, c) => total + (c.solde || 0), 0);

        // Alerte solde faible : moins de 2000 FCFA sur une carte active
        this.cartesFaibles = cartes.filter(c => c.statut === 'ACTIVE' && (c.solde || 0) < 2000);

        this.chargement = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.chargement = false;
        this.cdr.detectChanges();
      }
    });
  }
}