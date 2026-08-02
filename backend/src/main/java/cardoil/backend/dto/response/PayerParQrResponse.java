package cardoil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PayerParQrResponse {
    private Long transactionId;
    private String reference;
    private BigDecimal montant;
    private String produitNom;
    // Nom du client OU de l'employé selon le porteur résolu — générique
    // volontairement, contrairement à AchatCarteResponse.employeNom qui
    // suppose toujours un employé.
    private String porteurNom;
    private String stationNom;
    private BigDecimal nouveauSolde;
    private String statut;
    private LocalDateTime dateTransaction;
    // 0 pour un employé — pas de points fidélité, cohérent avec
    // confirmerTransactionEmploye/payerParCarte.
    private Integer pointsGagnes;
}