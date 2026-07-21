import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export type TypeCarteEmploye = 'RECHARGEABLE_LIBRE' | 'DOTATION_PLAFONNEE' | 'DOTATION_AVEC_REPORT';
export type StatutCarte = 'ACTIVE' | 'SUSPENDUE' | 'BLOQUEE' | 'EXPIREE';
export type TypeRecharge = 'MANUELLE' | 'GROUPEE' | 'DOTATION';

export interface Carte {
  id?: number;
  numeroCarte?: string;
  employeId: number;
  employeNomComplet?: string;
  matricule?: string;
  typeCarte: TypeCarteEmploye;
  solde?: number;
  statut?: StatutCarte;
  dateCreation?: string;
  dateExpiration?: string | null;
  montantDotationMensuelle?: number | null;
  dateRenouvellement?: number | null;
  plafondCumuleMax?: number | null;
  sourceFinancement?: string;
}

export interface Recharge {
  id: number;
  numeroCarte: string;
  montant: number;
  dateRecharge: string;
  effectuePar: string;
  type: TypeRecharge;
}

export interface LigneRecharge {
  numeroCarte: string;
  montant: number;
  commentaire?: string;
}

export interface RechargeGroupee {
  id: number;
  nomFichier: string;
  dateExecution: string;
  effectuePar: string;
  nombreReussies: number;
  nombreEchecs: number;
  montantTotal: number;
  detailsErreurs: string | null;
}

@Injectable({ providedIn: 'root' })
export class CarteService {
  private apiUrl = `${environment.apiUrl}/admin/cartes`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Carte[]> {
    return this.http.get<Carte[]>(this.apiUrl);
  }

  create(carte: Carte): Observable<Carte> {
    return this.http.post<Carte>(this.apiUrl, carte);
  }

  changerStatut(id: number, statut: StatutCarte): Observable<Carte> {
    return this.http.patch<Carte>(`${this.apiUrl}/${id}/statut`, { statut });
  }

  recharger(id: number, montant: number): Observable<Recharge> {
    return this.http.post<Recharge>(`${this.apiUrl}/${id}/recharger`, { montant });
  }

  historique(id: number): Observable<Recharge[]> {
    return this.http.get<Recharge[]>(`${this.apiUrl}/${id}/recharges`);
  }

  renouveler(id: number): Observable<Carte> {
    return this.http.post<Carte>(`${this.apiUrl}/${id}/renouveler`, {});
  }

  rechargerGroupe(nomFichier: string, lignes: LigneRecharge[]): Observable<RechargeGroupee> {
    return this.http.post<RechargeGroupee>(`${this.apiUrl}/recharge-groupee`, { nomFichier, lignes });
  }

  historiqueGroupe(): Observable<RechargeGroupee[]> {
    return this.http.get<RechargeGroupee[]>(`${this.apiUrl}/recharges-groupees`);
  }
}