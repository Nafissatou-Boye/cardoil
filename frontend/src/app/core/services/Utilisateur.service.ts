import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Carte, Recharge } from './carte.service';

export interface UtilisateurListItem {
  id: number;
  login: string;
  nom: string;
  prenom: string;
  email: string;
  role: string;
  actif: boolean;
  departementNom: string | null;
  matricule: string | null;
  possedeUneCarte: boolean;
}

export interface UtilisateurDetail {
  id: number;
  login: string;
  nom: string;
  prenom: string;
  email: string;
  telephone: string | null;
  role: string;
  actif: boolean;
  dateCreation: string;
  derniereConnexion: string | null;
  matricule: string | null;
  departementNom: string | null;
  carte: Carte | null;
  historiqueRecharges: Recharge[] | null;
  departementGereNom: string | null;
  nombreEmployesDepartementGere: number | null;
}

@Injectable({ providedIn: 'root' })
export class UtilisateurService {
  private apiUrl = `${environment.apiUrl}/admin/utilisateurs`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<UtilisateurListItem[]> {
    return this.http.get<UtilisateurListItem[]>(this.apiUrl);
  }

  getDetail(id: number): Observable<UtilisateurDetail> {
    return this.http.get<UtilisateurDetail>(`${this.apiUrl}/${id}`);
  }
}