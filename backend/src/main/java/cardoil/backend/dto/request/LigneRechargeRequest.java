package cardoil.backend.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LigneRechargeRequest {
    private String numeroCarte;
    private BigDecimal montant;
    private String commentaire;
}