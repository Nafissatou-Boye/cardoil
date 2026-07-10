export type TypeEtablissement =
  | 'BANQUE'
  | 'FINTECH'
  | 'OPERATEUR_MOBILE_MONEY'
  | 'ETABLISSEMENT_MONNAIE_ELECTRONIQUE';

export type StatutEtablissement = 'ACTIF' | 'SUSPENDU' | 'INACTIF';

export interface EtablissementFinancier {
  id: number;
  nom: string;
  code: string;
  type: TypeEtablissement;
  statut: StatutEtablissement;
  apiKeyPrefix: string;
  apiKeyExpiration: string;
  rateLimitParMinute: number;
  emailContact: string;
  dateCreation: string;
  nombreCompagniesLiees: number;
}

export interface EtablissementFinancierCreate {
  nom: string;
  code: string;
  type: TypeEtablissement;
  emailContact: string;
  telephoneContact?: string;
  rateLimitParMinute?: number;
}

export interface ApiKeyGeneree {
  etablissementId: number;
  apiKey: string;
  apiKeyPrefix: string;
}

export const LABELS_TYPE_ETABLISSEMENT: Record<TypeEtablissement, string> = {
  BANQUE: 'Banque',
  FINTECH: 'Fintech',
  OPERATEUR_MOBILE_MONEY: 'Opérateur Mobile Money',
  ETABLISSEMENT_MONNAIE_ELECTRONIQUE: 'Établissement de Monnaie Électronique'
};