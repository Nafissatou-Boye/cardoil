package cardoil.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoriqueTransactionResponse {
    private Long id;
    private String reference;
    private BigDecimal montant;
    private String type;              // "ACHAT" ou "RECHARGE"
    private String statut;            // "REUSSIE", "ECHEC", "EN_ATTENTE", "ANNULEE"
    private String produitOuServiceNom;
    private String stationNom;
    private LocalDateTime dateTransaction;
    private Integer pointsGagnes;
}