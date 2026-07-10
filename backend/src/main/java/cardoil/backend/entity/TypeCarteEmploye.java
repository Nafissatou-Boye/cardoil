package cardoil.backend.entity;

public enum TypeCarteEmploye {
    RECHARGEABLE_LIBRE,      // Recharge à tout moment, pas de plafond
    DOTATION_PLAFONNEE,      // Montant fixe chaque mois, solde perdu
    DOTATION_AVEC_REPORT     // Dotation mensuelle + reliquat M-1
}