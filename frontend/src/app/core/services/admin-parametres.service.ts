import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface CompagnieProfil {
  id: number;
  nom: string;
  code: string;
  adresse: string;
  telephone: string;
  email: string;
  logo: string | null;
  paysNom: string;
  dateCreation: string;
}

export interface CompagnieProfilRequest {
  adresse: string;
  telephone: string;
  email: string;
  logo: string | null;
}

@Injectable({ providedIn: 'root' })
export class AdminParametresService {

  private apiUrl = `${environment.apiUrl}/admin/parametres`;

  constructor(private http: HttpClient) {}

  getProfil(): Observable<CompagnieProfil> {
    return this.http.get<CompagnieProfil>(`${this.apiUrl}/compagnie`);
  }

  updateProfil(profil: CompagnieProfilRequest): Observable<CompagnieProfil> {
    return this.http.put<CompagnieProfil>(`${this.apiUrl}/compagnie`, profil);
  }
}