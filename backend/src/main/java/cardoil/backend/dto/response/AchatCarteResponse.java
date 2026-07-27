package cardoil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AchatCarteResponse {
    private Long transactionId;
    private String reference;
    private BigDecimal montant;
    private String produitNom;
    private String employeNom;
    private String carteMasquee;
    private BigDecimal nouveauSolde;
    private String statut;
    private String stationNom;
    private LocalDateTime dateTransaction;
}