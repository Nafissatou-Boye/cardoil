// src/app/core/services/admin-transaction.service.ts

import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface TransactionLigne {
  dateTransaction: string;
  type: string;
  montant: number;
  station: string;
  operateur: string;
  statut: string;
}

@Injectable({ providedIn: 'root' })
export class AdminTransactionService {
  private apiUrl = `${environment.apiUrl}/admin/transactions`;

  constructor(private http: HttpClient) {}

  getTransactions(periode: string, stationId?: number | null): Observable<TransactionLigne[]> {
    let params = new HttpParams().set('periode', periode);
    if (stationId != null) {
      params = params.set('stationId', stationId.toString());
    }
    return this.http.get<TransactionLigne[]>(this.apiUrl, { params });
  }
}