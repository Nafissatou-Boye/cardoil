

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ServiceCatalogue {
  id: number;
  code: string;
  nom: string;
  categorie: string;
  description: string | null;
  prix: number | null;
  icone: string | null;
  couleurHex: string | null;
  statut: string;
  obligatoire: boolean;
  ordreTri: number;
  compagnieId: number;
  compagnieNom: string;
  stationIds: number[];
  stationNoms: string[];
}

export interface ServiceCatalogueRequest {
  code: string;
  nom: string;
  categorie: string;
  description?: string;
  prix?: number;
  icone?: string;
  couleurHex?: string;
  obligatoire: boolean;
  ordreTri: number;
}

@Injectable({ providedIn: 'root' })
export class ServiceCatalogueService {
  private apiUrl = `${environment.apiUrl}/admin/services`;

  constructor(private http: HttpClient) {}

  getServices(): Observable<ServiceCatalogue[]> {
    return this.http.get<ServiceCatalogue[]>(this.apiUrl);
  }

  getService(id: number): Observable<ServiceCatalogue> {
    return this.http.get<ServiceCatalogue>(`${this.apiUrl}/${id}`);
  }

  creerService(request: ServiceCatalogueRequest): Observable<ServiceCatalogue> {
    return this.http.post<ServiceCatalogue>(this.apiUrl, request);
  }

  modifierService(id: number, request: ServiceCatalogueRequest): Observable<ServiceCatalogue> {
    return this.http.put<ServiceCatalogue>(`${this.apiUrl}/${id}`, request);
  }

  changerStatut(id: number, statut: string): Observable<ServiceCatalogue> {
    return this.http.patch<ServiceCatalogue>(`${this.apiUrl}/${id}/statut`, { statut });
  }

  assignerStations(id: number, stationIds: number[]): Observable<ServiceCatalogue> {
    return this.http.put<ServiceCatalogue>(`${this.apiUrl}/${id}/stations`, { stationIds });
  }


  uploaderIcone(fichier: File): Observable<{ url: string }> {
    const formData = new FormData();
    formData.append('fichier', fichier);
    return this.http.post<{ url: string }>(`${environment.apiUrl}/admin/upload/icone`, formData);
  }
}