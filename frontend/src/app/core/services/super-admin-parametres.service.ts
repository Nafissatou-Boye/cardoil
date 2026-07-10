import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface SuperAdminProfil {
  id: number;
  login: string;
  nom: string;
  prenom: string;
  email: string;
  dateCreation: string;
}

export interface SuperAdminProfilRequest {
  nom: string;
  prenom: string;
  email: string;
}

@Injectable({ providedIn: 'root' })
export class SuperAdminParametresService {

  private apiUrl = `${environment.apiUrl}/super-admin/parametres`;

  constructor(private http: HttpClient) {}

  getProfil(): Observable<SuperAdminProfil> {
    return this.http.get<SuperAdminProfil>(`${this.apiUrl}/profil`);
  }

  updateProfil(request: SuperAdminProfilRequest): Observable<SuperAdminProfil> {
    return this.http.put<SuperAdminProfil>(`${this.apiUrl}/profil`, request);
  }
}