package cardoil.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompteEmployeResponse {

    // Identité
    private String nomComplet;
    private String matricule;
    private Long entrepriseId;
    private String entrepriseNom;

    // Carte
    private String numeroCarte;
    private String typeCarte;   // RECHARGEABLE_LIBRE | DOTATION_PLAFONNEE | DOTATION_AVEC_REPORT
    private BigDecimal solde;
    private String statut;

    // Nulles pour RECHARGEABLE_LIBRE ; renseignées pour les deux types DOTATION_*
    private BigDecimal montantDotationMensuelle;
    private Integer dateRenouvellement;

    // Uniquement pertinent pour DOTATION_AVEC_REPORT, peut rester null même dans ce cas (pas de plafond de cumul)
    private BigDecimal plafondCumuleMax;

    // Compagnie (via Entreprise.compagnie, jamais null — nullable=false sur l'entité)
    // et département (peut être null : employé rattaché directement à l'entreprise)
    private Long compagnieId;
    private String compagnieNom;
    private Long departementId;
    private String departementNom;
}