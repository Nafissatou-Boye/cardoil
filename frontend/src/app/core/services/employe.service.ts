import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Employe {
  id?: number;
  login?: string;
  matricule?: string;
  nom: string;
  prenom: string;
  email: string;
  telephone?: string | null;
  actif?: boolean;
  dateCreation?: string;
  departementId?: number | null;
  departementNom?: string;
  entrepriseId?: number;
  entrepriseNom?: string;
  possedeUneCarte?: boolean;
  motDePasseTemporaire?: string;
}

export interface EmployeRequest {
  nom: string;
  prenom: string;
  email: string;
  telephone?: string | null;
  matricule?: string | null;
  departementId?: number | null;
}

@Injectable({ providedIn: 'root' })
export class EmployeService {
  private apiUrl = `${environment.apiUrl}/admin/employes`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Employe[]> {
    return this.http.get<Employe[]>(this.apiUrl);
  }

  create(employe: EmployeRequest): Observable<Employe> {
    return this.http.post<Employe>(this.apiUrl, employe);
  }

  update(id: number, employe: EmployeRequest): Observable<Employe> {
    return this.http.put<Employe>(`${this.apiUrl}/${id}`, employe);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}