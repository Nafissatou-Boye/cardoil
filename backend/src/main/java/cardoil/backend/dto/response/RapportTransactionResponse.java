package cardoil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RapportTransactionResponse {
    private String dateTransaction;
    private String type;
    private BigDecimal montant;
    private String station;
    private String operateur;
    private String statut;
}