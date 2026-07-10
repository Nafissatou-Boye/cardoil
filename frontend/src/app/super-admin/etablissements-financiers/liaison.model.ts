import { StatutEtablissement } from './etablissement-financier.model';

export interface Liaison {
  id: number;
  compagnieId: number;
  compagnieNom: string;
  devise: string; // 🆕
  statut: StatutEtablissement;
  montantMinimum: number;
  montantMaximumParTransaction: number;
  plafondJournalierParClient: number;
  dateActivation: string;
}

// Utilisé pour la création (compagnieId requis) ET la modification (compagnieId ignoré côté backend).
export interface LiaisonRequest {
  compagnieId?: number;
  montantMinimum?: number;
  montantMaximumParTransaction?: number;
  plafondJournalierParClient?: number;
}

export interface CompagnieOption {
  id: number;
  nom: string;
}