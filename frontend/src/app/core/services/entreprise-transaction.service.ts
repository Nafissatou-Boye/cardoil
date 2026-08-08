import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface TransactionEmployeLigne {
  dateTransaction: string;
  type: string;
  montant: number;
  employeNom: string;
  station: string;
  statut: string;
}

@Injectable({ providedIn: 'root' })
export class EntrepriseTransactionService {

  private apiUrl = `${environment.apiUrl}/entreprise/transactions`;

  constructor(private http: HttpClient) {}

  getTransactions(periode: string): Observable<TransactionEmployeLigne[]> {
    return this.http.get<TransactionEmployeLigne[]>(this.apiUrl, {
      params: { periode },
    });
  }
}