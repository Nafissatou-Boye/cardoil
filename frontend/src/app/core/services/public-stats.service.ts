// src/app/core/services/public-stats.service.ts
//
// Pas d'intercepteur d'auth nécessaire ici — endpoint public, accessible
// avant connexion (voir SecurityConfig, /api/public/** en permitAll).

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface PublicStats {
  totalCompagnies: number;
  totalStations: number;
  volumeTraite: number;
}

@Injectable({ providedIn: 'root' })
export class PublicStatsService {
  private apiUrl = `${environment.apiUrl}/public/stats`;

  constructor(private http: HttpClient) {}

  getStats(): Observable<PublicStats> {
    return this.http.get<PublicStats>(this.apiUrl);
  }
}