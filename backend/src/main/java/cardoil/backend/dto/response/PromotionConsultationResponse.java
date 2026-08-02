package cardoil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

// ✅ Dédié à la consultation (Client/Employé) — distinct de PromotionResponse
// (admin), qui formate les dates en "dd/MM/yyyy HH:mm" pour l'affichage
// (DateTime.tryParse() de Flutter attend de l'ISO-8601, pas ce format).
// Noms alignés sur PromotionModel.fromJson (Flutter), vu en entier.
//
// pointsMultiplier/pointsRequired (Flutter) : aucun équivalent réel côté
// backend (la mécanique réelle est pointsParTranche/montantParTranche, un
// taux, pas un multiplicateur) — volontairement omis, repli sûr (0) côté
// Flutter plutôt qu'une correspondance inventée.
@Data
@Builder
public class PromotionConsultationResponse {
    private Long id;
    private String name;
    private String description;
    private String startDate;
    private String endDate;
    private String status;
    private String type;
    private BigDecimal minPurchaseAmount;
    private Long companyId;
}