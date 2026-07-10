package cardoil.backend.entity;

public enum TypeQr {
    STATIQUE,   // Carte physique, token fixe, nécessite PIN
    DYNAMIQUE   // Application mobile, token temporaire, sans PIN
}