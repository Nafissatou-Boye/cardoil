import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Employe } from './employe.service';
import { Carte, Recharge, TypeCarteEmploye, StatutCarte } from './carte.service';

export interface EmployeDepartementRequest {
  nom: string;
  prenom: string;
  email: string;
  telephone?: string | null;
  matricule?: string | null;
}

export interface EspaceDepartementInfo {
  departementId: number;
  departementNom: string;
  entrepriseNom: string;
  budgetDisponible: number;
  nombreEmployes: number;
  cartesActives: number;
  cartesSuspendues: number;
  cartesBloquees: number;
  cartesExpirees: number;
  soldeTotalCartes: number;
}

@Injectable({ providedIn: 'root' })
export class EspaceDepartementService {
  private apiUrl = `${environment.apiUrl}/departement`;

  constructor(private http: HttpClient) {}

  getInfo(): Observable<EspaceDepartementInfo> {
    return this.http.get<EspaceDepartementInfo>(`${this.apiUrl}/info`);
  }

  getEmployes(): Observable<Employe[]> {
    return this.http.get<Employe[]>(`${this.apiUrl}/employes`);
  }

  createEmploye(request: EmployeDepartementRequest): Observable<Employe> {
    return this.http.post<Employe>(`${this.apiUrl}/employes`, request);
  }

  updateEmploye(id: number, request: EmployeDepartementRequest): Observable<Employe> {
    return this.http.put<Employe>(`${this.apiUrl}/employes/${id}`, request);
  }

  deleteEmploye(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/employes/${id}`);
  }

  getCartes(): Observable<Carte[]> {
    return this.http.get<Carte[]>(`${this.apiUrl}/cartes`);
  }

  createCarte(carte: Carte): Observable<Carte> {
    return this.http.post<Carte>(`${this.apiUrl}/cartes`, carte);
  }

  changerStatutCarte(id: number, statut: StatutCarte): Observable<Carte> {
    return this.http.patch<Carte>(`${this.apiUrl}/cartes/${id}/statut`, { statut });
  }

  rechargerCarte(id: number, montant: number): Observable<Recharge> {
    return this.http.post<Recharge>(`${this.apiUrl}/cartes/${id}/recharger`, { montant });
  }

  historiqueRecharges(id: number): Observable<Recharge[]> {
    return this.http.get<Recharge[]>(`${this.apiUrl}/cartes/${id}/recharges`);
  }
}