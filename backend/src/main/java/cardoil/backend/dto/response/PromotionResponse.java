package cardoil.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionResponse {

    private Long id;
    private String nom;
    private String description;
    
    private String imageUrl;
    private String type;
    private String statut;
    private String dateDebut;
    private String dateFin;
    private long joursRestants;
    private long dureeJours;

    // Éligibilité
    private BigDecimal montantMinimum;
    private List<StationInfo> stationsConcernees;

    // Limites
    private Integer plafondParClient;
    private Integer plafondGlobal;
    private Integer plafondJournalier;

    // Récompenses POINTS
    private Integer pointsParTranche;
    private BigDecimal montantParTranche;

    // Récompenses GIFT
    private String descriptionCadeau;
    private Integer stockCadeaux;

    // Récompenses SCRATCH
    private BigDecimal probabiliteGain;
    private String descriptionLot;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StationInfo {
        private Long id;
        private String nom;
    }
}