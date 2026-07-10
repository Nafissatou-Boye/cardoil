import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface AdminCompagnie {
  id?: number;
  login?: string;
  nom: string;
  prenom: string;
  email: string;
  actif: boolean;
  compagnieNom?: string;
  compagnieId: number | null;
  dateCreation?: string;
  derniereConnexion?: string;
}

@Injectable({ providedIn: 'root' })
export class SuperAdminUtilisateurService {

  private apiUrl = `${environment.apiUrl}/super-admin/utilisateurs`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<AdminCompagnie[]> {
    return this.http.get<AdminCompagnie[]>(this.apiUrl);
  }

  create(admin: AdminCompagnie): Observable<AdminCompagnie> {
    return this.http.post<AdminCompagnie>(this.apiUrl, admin);
  }

  update(id: number, admin: AdminCompagnie): Observable<AdminCompagnie> {
    return this.http.put<AdminCompagnie>(`${this.apiUrl}/${id}`, admin);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  toggleActif(id: number): Observable<AdminCompagnie> {
    return this.http.patch<AdminCompagnie>(`${this.apiUrl}/${id}/toggle`, {});
  }
}