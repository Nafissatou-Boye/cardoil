// RechargeClientResponse.java — inchangé par rapport à avant
package cardoil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class RechargeClientResponse {
    private Long transactionId;
    private String reference;
    private BigDecimal montant;
    private String clientNom;
    private String telephoneMasque;
    private BigDecimal nouveauSolde;
    private String statut;
    private String stationNom;
    private LocalDateTime dateTransaction;
}