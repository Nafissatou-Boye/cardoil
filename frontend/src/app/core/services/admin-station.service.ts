import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Station } from './station.service';

@Injectable({ providedIn: 'root' })
export class AdminStationService {

  private apiUrl = `${environment.apiUrl}/admin/stations`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Station[]> {
    return this.http.get<Station[]>(this.apiUrl);
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