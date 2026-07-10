import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface GerantDashboard {
  stationId: number;
  stationNom: string;
  stationAdresse: string;
  caJour: number;
  nbTransactionsJour: number;
  transactionsReussiesJour: number;
  transactionsEchecJour: number;
  dernieresTransactions: TransactionItem[];
}

export interface TransactionItem {
  id: number;
  dateTransaction: string;
  montant: number;
  type: string;
  statut: string;
  produitNom: string | null;
  prixTtc: number | null;
}

export interface ProduitDisponible {
  id: number;
  nom: string;
  type: string;
  prixTtcActuel: number | null;
}

export interface TransactionRequest {
  type: string;
  produitId: number | null;
  montant: number | null;
}

@Injectable({ providedIn: 'root' })
export class GerantService {

  private apiUrl = `${environment.apiUrl}/gerant`;

  constructor(private http: HttpClient) {}

  getDashboard(): Observable<GerantDashboard> {
    return this.http.get<GerantDashboard>(`${this.apiUrl}/dashboard`);
  }

  getProduitsDisponibles(): Observable<ProduitDisponible[]> {
    return this.http.get<ProduitDisponible[]>(`${this.apiUrl}/transactions/produits-disponibles`);
  }

  getTransactionsRecentes(): Observable<TransactionItem[]> {
    return this.http.get<TransactionItem[]>(`${this.apiUrl}/transactions`);
  }

  creerTransaction(request: TransactionRequest): Observable<TransactionItem> {
    return this.http.post<TransactionItem>(`${this.apiUrl}/transactions`, request);
  }
}