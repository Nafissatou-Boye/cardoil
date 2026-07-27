// NotificationPersonnelleResponse.java
package cardoil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class NotificationPersonnelleResponse {
    private Long id;
    private String titre;
    private String message;
    private String type;
    private boolean lu;
    private LocalDateTime dateCreation;
    private LocalDateTime dateLecture;

    // Enrichissement optionnel depuis la transaction liée
    private Long transactionId;
    private BigDecimal montant;
    private String stationNom;
    private String produitNom;
    private BigDecimal nouveauSolde;
}