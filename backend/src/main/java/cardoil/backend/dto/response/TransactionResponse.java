package cardoil.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private Long id;
    private String dateTransaction;
    private BigDecimal montant;
    private String type;
    private String statut;
    private String produitNom;
    private BigDecimal prixTtc;
}