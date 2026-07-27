package cardoil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatutTransactionResponse {
    private Long transactionId;
    private String statut; 
    private String clientNom; 
    private String reference;
}