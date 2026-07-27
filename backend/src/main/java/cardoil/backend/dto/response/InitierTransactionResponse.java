package cardoil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class InitierTransactionResponse {
    private Long transactionId;
    private String codeConfirmation; // ← donnée à encoder dans le QR affiché par le gérant
    private LocalDateTime expiration;
    private BigDecimal montant;
    private String produitNom;
    private String stationNom;
}