import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

// Produit : ajouter les 4 champs Produit
export interface Produit {
  id?: number;
  nom: string;
  type: string;
  statut?: string;
  description: string;
  obligatoire: boolean;
  categorie?: string;                      // 🆕
  unite?: string;                          // 🆕
  commissionFixe?: number | null;          // 🆕
  commissionPourcentage?: number | null;   // 🆕
  prixTtcActuel?: number | null;
  prixHtvaActuel?: number | null;
  prixHttActuel?: number | null;
  datePrixActuel?: string | null;
}

// PrixRequest : ajouter les 2 dates
export interface PrixRequest {
  prixTtc: number | null;
  prixHtva: number | null;
  prixHtt: number | null;
  dateDebut?: string | null; // 🆕 format ISO "YYYY-MM-DD"
  dateFin?: string | null;   // 🆕
}

// 🆕 nouveau type, remplace PrixJour comme retour de definirPrix
export interface PrixProduit {
  id: number;
  produitId: number;
  produitNom: string;
  prixTtc: number | null;
  prixHtva: number | null;
  prixHtt: number | null;
  dateDebut: string;
  dateFin: string | null;
  enVigueurAujourdHui: boolean;
}

export interface PrixRequest {
  prixTtc: number | null;
  prixHtva: number | null;
  prixHtt: number | null;
}

export interface PrixJour {
  id: number;
  prixTtc: number | null;
  prixHtva: number | null;
  prixHtt: number | null;
  datePrix: string;
}

@Injectable({ providedIn: 'root' })
export class AdminProduitService {

  private apiUrl = `${environment.apiUrl}/admin/produits`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Produit[]> {
    return this.http.get<Produit[]>(this.apiUrl);
  }

  create(produit: Produit): Observable<Produit> {
    return this.http.post<Produit>(this.apiUrl, produit);
  }

  update(id: number, produit: Produit): Observable<Produit> {
    return this.http.put<Produit>(`${this.apiUrl}/${id}`, produit);
  }

  changerStatut(id: number, statut: string): Observable<Produit> {
    return this.http.patch<Produit>(`${this.apiUrl}/${id}/statut`, { statut });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

 definirPrix(produitId: number, prix: PrixRequest): Observable<PrixProduit> {
  return this.http.post<PrixProduit>(`${this.apiUrl}/${produitId}/prix`, prix);
}

  getHistoriquePrix(produitId: number): Observable<PrixJour[]> {
    return this.http.get<PrixJour[]>(`${this.apiUrl}/${produitId}/prix/historique`);
  }

  // ajouter dans la classe AdminProduitService, à côté de getHistoriquePrix
getProgrammationPrix(produitId: number): Observable<PrixProduit[]> {
  return this.http.get<PrixProduit[]>(`${this.apiUrl}/${produitId}/prix/programmation`);
}
}