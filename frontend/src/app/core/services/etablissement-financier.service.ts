import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  EtablissementFinancier,
  EtablissementFinancierCreate,
  ApiKeyGeneree,
  StatutEtablissement
} from '../../super-admin/etablissements-financiers/etablissement-financier.model';

@Injectable({ providedIn: 'root' })
export class EtablissementFinancierService {

 private readonly baseUrl = `${environment.apiUrl}/super-admin/etablissements-financiers`;

  constructor(private http: HttpClient) {}

  lister(): Observable<EtablissementFinancier[]> {
    return this.http.get<EtablissementFinancier[]>(this.baseUrl);
  }

  creer(dto: EtablissementFinancierCreate): Observable<ApiKeyGeneree> {
    return this.http.post<ApiKeyGeneree>(this.baseUrl, dto);
  }

  regenererCle(id: number): Observable<ApiKeyGeneree> {
    return this.http.post<ApiKeyGeneree>(`${this.baseUrl}/${id}/rotation-cle`, {});
  }

  changerStatut(id: number, statut: StatutEtablissement): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/${id}/statut?statut=${statut}`, {});
  }
}