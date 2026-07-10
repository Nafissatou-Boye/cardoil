import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface StationInfo {
  id: number;
  nom: string;
}

export interface Personnel {
  id?: number;
  login: string;
  motDePasse?: string;
  nom: string;
  prenom: string;
  email: string;
  role?: string;
  actif: boolean;
  dateCreation?: string;
  derniereConnexion?: string | null;
  station?: StationInfo | null;
  stationId?: number | null;
}

@Injectable({ providedIn: 'root' })
export class AdminPersonnelService {

  private apiUrl = `${environment.apiUrl}/admin/personnel`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Personnel[]> {
    return this.http.get<Personnel[]>(this.apiUrl);
  }

  create(personnel: Personnel): Observable<Personnel> {
    return this.http.post<Personnel>(this.apiUrl, personnel);
  }

  update(id: number, personnel: Personnel): Observable<Personnel> {
    return this.http.put<Personnel>(`${this.apiUrl}/${id}`, personnel);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}