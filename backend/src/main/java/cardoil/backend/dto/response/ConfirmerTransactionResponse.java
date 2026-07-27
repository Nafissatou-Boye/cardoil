package cardoil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ConfirmerTransactionResponse {
    private Long transactionId;
    private String reference;
    private BigDecimal montant;
    private String produitNom;
    private String stationNom;
    private BigDecimal nouveauSolde;
    private String statut;
    private LocalDateTime dateTransaction;
    private int pointsGagnes;
}