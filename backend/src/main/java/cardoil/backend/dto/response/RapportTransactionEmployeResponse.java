package cardoil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RapportTransactionEmployeResponse {
    private String dateTransaction;
    private String type;
    private BigDecimal montant;
    private String employeNom;
    private String station;
    private String statut;
}