import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Liaison, LiaisonRequest, CompagnieOption } from './liaison.model';
import { StatutEtablissement } from './etablissement-financier.model';

@Injectable({ providedIn: 'root' })
export class LiaisonService {

  private readonly baseUrl = `${environment.apiUrl}/super-admin/etablissements-financiers`;

  constructor(private http: HttpClient) {}

  lister(etablissementId: number): Observable<Liaison[]> {
    return this.http.get<Liaison[]>(`${this.baseUrl}/${etablissementId}/compagnies`);
  }

  creer(etablissementId: number, dto: LiaisonRequest): Observable<Liaison> {
    return this.http.post<Liaison>(`${this.baseUrl}/${etablissementId}/compagnies`, dto);
  }

  modifierPlafonds(etablissementId: number, liaisonId: number, dto: LiaisonRequest): Observable<Liaison> {
    return this.http.patch<Liaison>(`${this.baseUrl}/${etablissementId}/compagnies/${liaisonId}`, dto);
  }

  changerStatut(etablissementId: number, liaisonId: number, statut: StatutEtablissement): Observable<Liaison> {
    return this.http.patch<Liaison>(
      `${this.baseUrl}/${etablissementId}/compagnies/${liaisonId}/statut?statut=${statut}`, {}
    );
  }

  supprimer(etablissementId: number, liaisonId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${etablissementId}/compagnies/${liaisonId}`);
  }

 listerCompagniesDisponibles(): Observable<CompagnieOption[]> {
  return this.http.get<CompagnieOption[]>(`${environment.apiUrl}/compagnies`);
}
}