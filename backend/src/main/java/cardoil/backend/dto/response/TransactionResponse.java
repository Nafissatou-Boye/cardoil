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
    private String reference;
    private String dateTransaction;
    private BigDecimal montant;
    private String type;
    private String statut;
    private String produitNom;
    private BigDecimal prixTtc;

    private String clientNomComplet;
    private String clientIdentifiantMasque; // ex: 77*****83 ou ************0041
    private String clientType;              // "EMPLOYE" ou "CLIENT_PARTICULIER"
}