// AnnulerTransactionResponse.java
package cardoil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AnnulerTransactionResponse {
    private Long transactionId;
    private String reference;
    private BigDecimal montant;
    private String statut;
    private LocalDateTime dateAnnulation;
    private String motif;
}