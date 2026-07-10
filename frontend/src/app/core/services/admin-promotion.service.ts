import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface StationInfo {
  id: number;
  nom: string;
}

export interface Promotion {
  id?: number;
  nom: string;
  description: string;
  type: string;
  statut?: string;
  dateDebut: string;
  dateFin: string;
  joursRestants?: number;
  dureeJours?: number;

  // Éligibilité
  montantMinimum: number | null;
  stationsConcernees?: StationInfo[];
  stationIds?: number[];

  // Limites
  plafondParClient: number | null;
  plafondGlobal: number | null;
  plafondJournalier: number | null;

  // POINTS
  pointsParTranche: number | null;
  montantParTranche: number | null;

  // GIFT
  descriptionCadeau: string | null;
  stockCadeaux: number | null;

  // SCRATCH
  probabiliteGain: number | null;
  descriptionLot: string | null;
}

@Injectable({ providedIn: 'root' })
export class AdminPromotionService {

  private apiUrl = `${environment.apiUrl}/admin/promotions`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Promotion[]> {
    return this.http.get<Promotion[]>(this.apiUrl);
  }

  create(promotion: Promotion): Observable<Promotion> {
    return this.http.post<Promotion>(this.apiUrl, promotion);
  }

  update(id: number, promotion: Promotion): Observable<Promotion> {
    return this.http.put<Promotion>(`${this.apiUrl}/${id}`, promotion);
  }

  changerStatut(id: number, statut: string): Observable<Promotion> {
    return this.http.patch<Promotion>(`${this.apiUrl}/${id}/statut`, { statut });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}