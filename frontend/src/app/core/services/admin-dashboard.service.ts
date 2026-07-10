import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface DashboardAdminCompagnie {
  nomCompagnie: string;
  codeCompagnie: string;
  totalStations: number;
  stationsActives: number;
  totalEmployes: number;
}

@Injectable({ providedIn: 'root' })
export class AdminDashboardService {

  private apiUrl = `${environment.apiUrl}/admin`;

  constructor(private http: HttpClient) {}

  getDashboard(): Observable<DashboardAdminCompagnie> {
    return this.http.get<DashboardAdminCompagnie>(`${this.apiUrl}/dashboard`);
  }
}