import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Recharge, TypeCarteEmploye, StatutCarte } from './carte.service';

export interface RapportDepartement {
  departementId: number;
  departementNom: string;
  budgetDisponible: number;
  nombreEmployes: number;
  soldeCumuleCartes: number;
}

export interface RapportEmploye {
  employeId: number;
  nomComplet: string;
  matricule: string;
  departementNom: string;
  numeroCarte: string | null;
  typeCarte: TypeCarteEmploye | null;
  statutCarte: StatutCarte | null;
  soldeActuel: number;
  totalCredite: number;
}

export interface SuiviBudget {
  soldeDisponibleEntreprise: number;
  totalBudgetAlloueDepartements: number;
  totalSoldeCartesActives: number;
}

@Injectable({ providedIn: 'root' })
export class RapportService {
  private apiUrl = `${environment.apiUrl}/admin/rapports`;

  constructor(private http: HttpClient) {}

  getRapportDepartements(): Observable<RapportDepartement[]> {
    return this.http.get<RapportDepartement[]>(`${this.apiUrl}/departements`);
  }

  getRapportEmployes(): Observable<RapportEmploye[]> {
    return this.http.get<RapportEmploye[]>(`${this.apiUrl}/employes`);
  }

  getSuiviBudget(): Observable<SuiviBudget> {
    return this.http.get<SuiviBudget>(`${this.apiUrl}/budget`);
  }

  getHistoriqueGlobal(): Observable<Recharge[]> {
    return this.http.get<Recharge[]>(`${this.apiUrl}/historique`);
  }
}