import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface CompagnieInfo {
  id: number;
  nom: string;
  code: string;
}

export interface Station {
  id?: number;
  nom: string;
  adresse: string;
  latitude: number | null;
  longitude: number | null;
  telephone: string;
  actif: boolean;
  dateCreation?: string;
  compagnie?: CompagnieInfo;
  compagnieId?: number;
  gerantId?: number;
  gerantNom?: string;
}

@Injectable({ providedIn: 'root' })
export class StationService {

  private apiUrl = `${environment.apiUrl}/stations`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Station[]> {
    return this.http.get<Station[]>(this.apiUrl);
  }

  getById(id: number): Observable<Station> {
    return this.http.get<Station>(`${this.apiUrl}/${id}`);
  }

  create(station: Station): Observable<Station> {
    return this.http.post<Station>(this.apiUrl, station);
  }

  update(id: number, station: Station): Observable<Station> {
    return this.http.put<Station>(`${this.apiUrl}/${id}`, station);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}