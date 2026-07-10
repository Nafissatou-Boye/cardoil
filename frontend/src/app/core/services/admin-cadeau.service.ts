import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Cadeau {
  id?: number;
  nom: string;
  type: string;
  coutEnPoints: number | null;
  stockDisponible: number | null;
  image: string | null;
  descriptionLongue: string | null;
  dateExpiration: string | null;
  actif: boolean;
  illimite?: boolean;
}

@Injectable({ providedIn: 'root' })
export class AdminCadeauService {

  private apiUrl = `${environment.apiUrl}/admin/cadeaux`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Cadeau[]> {
    return this.http.get<Cadeau[]>(this.apiUrl);
  }

  create(cadeau: Cadeau): Observable<Cadeau> {
    return this.http.post<Cadeau>(this.apiUrl, cadeau);
  }

  update(id: number, cadeau: Cadeau): Observable<Cadeau> {
    return this.http.put<Cadeau>(`${this.apiUrl}/${id}`, cadeau);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  toggleActif(id: number): Observable<Cadeau> {
    return this.http.patch<Cadeau>(`${this.apiUrl}/${id}/toggle`, {});
  }
}