import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface StationRapport {
  id: number;
  nom: string;
  ca: number;
  nbTransactions: number;
}

export interface TransactionRapport {
  id: number;
  dateTransaction: string;
  montant: number;
  type: string;
  statut: string;
  station: string;
  operateur: string;
}

export interface RapportGlobal {
  caTotal: number;
  totalTransactions: number;
  transactionsReussies: number;
  transactionsEchec: number;
  parStation: StationRapport[];
  dernieresTransactions: TransactionRapport[];
}

@Injectable({ providedIn: 'root' })
export class AdminRapportService {

  private apiUrl = `${environment.apiUrl}/admin/rapports`;

  constructor(private http: HttpClient) {}

  getRapport(periode: string): Observable<RapportGlobal> {
    return this.http.get<RapportGlobal>(`${this.apiUrl}?periode=${periode}`);
  }
}