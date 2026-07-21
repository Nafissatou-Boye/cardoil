import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Departement {
  id?: number;
  nom: string;
  description: string | null;
  budget: number;
  actif: boolean;
  nombreEmployes?: number;
}

export interface AdminDepartementInfo {
  id: number;
  login: string;
  motDePasseTemporaire?: string;
  nom: string;
  prenom: string;
  email: string;
  actif: boolean;
}

export interface AdminDepartementRequest {
  nom: string;
  prenom: string;
  email: string;
}

export interface EntrepriseInfo {
  id: number;
  nom: string;
  soldeDisponible: number;
}

@Injectable({ providedIn: 'root' })
export class DepartementService {
  private apiUrl = `${environment.apiUrl}/admin/departements`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Departement[]> {
    return this.http.get<Departement[]>(this.apiUrl);
  }

  create(departement: Departement): Observable<Departement> {
    return this.http.post<Departement>(this.apiUrl, departement);
  }

  update(id: number, departement: Departement): Observable<Departement> {
    return this.http.put<Departement>(`${this.apiUrl}/${id}`, departement);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  toggleActif(id: number): Observable<Departement> {
    return this.http.patch<Departement>(`${this.apiUrl}/${id}/toggle`, {});
  }

  getAdmin(departementId: number): Observable<AdminDepartementInfo | null> {
    return this.http.get<AdminDepartementInfo | null>(`${this.apiUrl}/${departementId}/admin`);
  }

  createAdmin(departementId: number, request: AdminDepartementRequest): Observable<AdminDepartementInfo> {
    return this.http.post<AdminDepartementInfo>(`${this.apiUrl}/${departementId}/admin`, request);
  }

  remplacerAdmin(departementId: number, request: AdminDepartementRequest): Observable<AdminDepartementInfo> {
    return this.http.put<AdminDepartementInfo>(`${this.apiUrl}/${departementId}/admin`, request);
  }

  getInfoEntreprise(): Observable<EntrepriseInfo> {
    return this.http.get<EntrepriseInfo>(`${this.apiUrl}/entreprise-info`);
  }

  crediterBudget(id: number, montant: number): Observable<Departement> {
    return this.http.post<Departement>(`${this.apiUrl}/${id}/crediter`, { montant });
  }
}