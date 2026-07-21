import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { UtilisateurService, UtilisateurListItem } from '../../core/services/Utilisateur.service';

@Component({
  selector: 'app-utilisateurs',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './utilisateurs.html',
  styleUrl: './utilisateurs.css'
})
export class UtilisateursComponent implements OnInit {

  listeUtilisateurs: UtilisateurListItem[] = [];
  chargement = true;
  filtreRole: 'TOUS' | 'EMPLOYE' | 'ADMIN_DEPARTEMENT' = 'TOUS';

  constructor(
    private utilisateurService: UtilisateurService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.chargement = true;
    this.utilisateurService.getAll().subscribe({
      next: (data) => {
        this.listeUtilisateurs = data;
        this.chargement = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.chargement = false;
        this.cdr.detectChanges();
      }
    });
  }

  get utilisateursFiltres(): UtilisateurListItem[] {
    if (this.filtreRole === 'TOUS') return this.listeUtilisateurs;
    return this.listeUtilisateurs.filter(u => u.role === this.filtreRole);
  }

  changerFiltre(role: 'TOUS' | 'EMPLOYE' | 'ADMIN_DEPARTEMENT'): void {
    this.filtreRole = role;
  }

  labelRole(role: string): string {
    return role === 'EMPLOYE' ? 'Employé' : (role === 'ADMIN_DEPARTEMENT' ? 'Admin Département' : role);
  }

  ouvrirDetail(utilisateur: UtilisateurListItem): void {
    this.router.navigate(['/entreprise/utilisateurs', utilisateur.id]);
  }
}