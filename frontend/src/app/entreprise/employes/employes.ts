import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EmployeService, Employe, EmployeRequest } from '../../core/services/employe.service';
import { DepartementService, Departement } from '../../core/services/departement.service';

@Component({
  selector: 'app-employes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './employes.html',
  styleUrl: './employes.css'
})
export class EmployesComponent implements OnInit {

  listeEmployes: Employe[] = [];
  chargement = true;
  erreur = '';
  enregistrement = false;

  modalOuvert = false;
  modeEdition = false;
  employeCourant: EmployeRequest = this.nouvelEmploye();
  employeIdEnEdition: number | null = null;

  dernierEmployeCree: Employe | null = null;

  listeDepartements: Departement[] = [];

  constructor(
    private employeService: EmployeService,
    private departementService: DepartementService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.charger();
    this.chargerDepartements();
  }

  chargerDepartements(): void {
    this.departementService.getAll().subscribe({
      next: (data) => {
        this.listeDepartements = data.filter(d => d.actif);
        this.cdr.detectChanges();
      },
      error: () => {}
    });
  }

  charger(): void {
    this.chargement = true;
    this.employeService.getAll().subscribe({
      next: (data) => {
        this.listeEmployes = data;
        this.chargement = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.chargement = false;
        this.cdr.detectChanges();
      }
    });
  }

  nouvelEmploye(): EmployeRequest {
    return {
      nom: '',
      prenom: '',
      email: '',
      telephone: null,
      matricule: null,
      departementId: null
    };
  }

  ouvrirAjout(): void {
    this.modeEdition = false;
    this.employeCourant = this.nouvelEmploye();
    this.employeIdEnEdition = null;
    this.dernierEmployeCree = null;
    this.erreur = '';
    this.enregistrement = false;
    this.modalOuvert = true;
  }

  ouvrirEdition(employe: Employe): void {
    this.modeEdition = true;
    this.employeIdEnEdition = employe.id ?? null;
    this.employeCourant = {
      nom: employe.nom,
      prenom: employe.prenom,
      email: employe.email,
      telephone: employe.telephone ?? null,
      matricule: employe.matricule ?? null,
      departementId: employe.departementId ?? null
    };
    this.dernierEmployeCree = null;
    this.erreur = '';
    this.enregistrement = false;
    this.modalOuvert = true;
  }

  fermerModal(): void {
    this.modalOuvert = false;
    this.dernierEmployeCree = null;
  }

  enregistrer(): void {
    if (this.enregistrement) return;
    this.enregistrement = true;
    this.erreur = '';

    const obs = this.modeEdition && this.employeIdEnEdition
      ? this.employeService.update(this.employeIdEnEdition, this.employeCourant)
      : this.employeService.create(this.employeCourant);

    obs.subscribe({
      next: (resultat) => {
        this.enregistrement = false;
        if (!this.modeEdition) {
          this.dernierEmployeCree = resultat;
          this.charger();
        } else {
          this.fermerModal();
          this.charger();
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.enregistrement = false;
        this.erreur = err.error?.erreur || err.error?.message || 'Une erreur est survenue';
        this.cdr.detectChanges();
      }
    });
  }

  supprimer(employe: Employe): void {
    if (!employe.id) return;
    if (!confirm(`Supprimer l'employé "${employe.prenom} ${employe.nom}" ?`)) return;
    this.employeService.delete(employe.id).subscribe({
      next: () => this.charger(),
      error: (err) => alert(err.error?.erreur || err.error?.message || 'Impossible de supprimer')
    });
  }
}