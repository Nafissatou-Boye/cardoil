import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Entreprise {
  id?: number;
  nom: string;
  code: string;
  secteurActivite: string | null;
  adresse: string | null;
  telephone: string | null;
  email: string | null;
  actif: boolean;
  dateCreation?: string;
  compagnieNom?: string;
  
}

export interface AdminEntrepriseInfo {
  id: number;
  login: string;
  motDePasseTemporaire?: string;
  nom: string;
  prenom: string;
  email: string;
  actif: boolean;
  
}

export interface AdminEntrepriseRequest {
  nom: string;
  prenom: string;
  email: string;
}

@Injectable({ providedIn: 'root' })
export class AdminEntrepriseService {

  private apiUrl = `${environment.apiUrl}/admin/entreprises`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Entreprise[]> {
    return this.http.get<Entreprise[]>(this.apiUrl);
  }

  create(entreprise: Entreprise): Observable<Entreprise> {
    return this.http.post<Entreprise>(this.apiUrl, entreprise);
  }

  update(id: number, entreprise: Entreprise): Observable<Entreprise> {
    return this.http.put<Entreprise>(`${this.apiUrl}/${id}`, entreprise);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  toggleActif(id: number): Observable<Entreprise> {
    return this.http.patch<Entreprise>(`${this.apiUrl}/${id}/toggle`, {});
  }

  getAdmin(entrepriseId: number): Observable<AdminEntrepriseInfo | null> {
  return this.http.get<AdminEntrepriseInfo | null>(`${this.apiUrl}/${entrepriseId}/admin`);
}

createAdmin(entrepriseId: number, request: AdminEntrepriseRequest): Observable<any> {
  return this.http.post(`${environment.apiUrl}/admin/personnel/admin-entreprise/${entrepriseId}`, request);
}

remplacerAdmin(entrepriseId: number, request: AdminEntrepriseRequest): Observable<any> {
  return this.http.put(`${environment.apiUrl}/admin/personnel/admin-entreprise/${entrepriseId}`, request);
}
}