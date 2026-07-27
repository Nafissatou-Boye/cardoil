// TransactionRecenteResponse.java
package cardoil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionRecenteResponse {
    private Long id;
    private String reference;
    private BigDecimal montant;
    private String type;
    private String statut;
    private String produitOuServiceNom;
    private String clientNom;
    private String operateurNom;
    private LocalDateTime dateTransaction;
    private boolean annulable;
}