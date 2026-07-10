package cardoil.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RechargeResponseDTO {
    private UUID transactionId;
    private String reference;
    private String status;
    private BigDecimal nouveauSolde; // optionnel selon accord contractuel
    private LocalDateTime processedAt;
    // ajouter ce champ
private String devise;
}