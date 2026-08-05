package cardoil.backend.dto.request;

import cardoil.backend.entity.TypePromotion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionRequest {

    // Étape 1
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;
    private String description;


    private String imageUrl;

    @NotNull(message = "Le type est obligatoire")
    private TypePromotion type;

    // Étape 2
    @NotNull(message = "La date de début est obligatoire")
    private LocalDateTime dateDebut;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDateTime dateFin;

    // Étape 3
    private BigDecimal montantMinimum;
    private List<Long> stationIds; // null = toutes les stations

    // Étape 4
    private Integer plafondParClient;
    private Integer plafondGlobal;
    private Integer plafondJournalier;

    // Étape 5 — POINTS
    private Integer pointsParTranche;
    private BigDecimal montantParTranche;

    // Étape 5 — GIFT
    private String descriptionCadeau;
    private Integer stockCadeaux;

    // Étape 5 — SCRATCH
    private BigDecimal probabiliteGain;
    private String descriptionLot;
}