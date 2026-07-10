import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface PaysInfo {
  id: number;
  nom: string;
  codeIso: string;
}

export interface Compagnie {
  id?: number;
  nom: string;
  code: string;
  logo?: string;
  adresse: string;
  telephone: string;
  email: string;
  actif: boolean;
  dateCreation?: string;
  pays?: PaysInfo;
  paysId?: number;
  nombreStations?: number;
  adminNom?: string;
}

@Injectable({ providedIn: 'root' })
export class CompagnieService {

  private apiUrl = `${environment.apiUrl}/compagnies`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Compagnie[]> {
    return this.http.get<Compagnie[]>(this.apiUrl);
  }

  getById(id: number): Observable<Compagnie> {
    return this.http.get<Compagnie>(`${this.apiUrl}/${id}`);
  }

  create(compagnie: Compagnie): Observable<Compagnie> {
    return this.http.post<Compagnie>(this.apiUrl, compagnie);
  }

  update(id: number, compagnie: Compagnie): Observable<Compagnie> {
    return this.http.put<Compagnie>(`${this.apiUrl}/${id}`, compagnie);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}