import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { UtilisateurService, UtilisateurDetail } from '../../core/services/Utilisateur.service';

@Component({
  selector: 'app-utilisateur-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './utilisateur-detail.html',
  styleUrl: './utilisateur-detail.css'
})
export class UtilisateurDetailComponent implements OnInit {

  utilisateur: UtilisateurDetail | null = null;
  chargement = true;
  erreur = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private utilisateurService: UtilisateurService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.router.navigate(['/entreprise/utilisateurs']);
      return;
    }
    this.charger(id);
  }

  charger(id: number): void {
    this.chargement = true;
    this.utilisateurService.getDetail(id).subscribe({
      next: (data) => {
        this.utilisateur = data;
        this.chargement = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.chargement = false;
        this.erreur = err.error?.erreur || err.error?.message || 'Utilisateur introuvable';
        this.cdr.detectChanges();
      }
    });
  }

  retour(): void {
    this.router.navigate(['/entreprise/utilisateurs']);
  }

  labelRole(role: string): string {
    return role === 'EMPLOYE' ? 'Employé' : (role === 'ADMIN_DEPARTEMENT' ? 'Admin Département' : role);
  }
}