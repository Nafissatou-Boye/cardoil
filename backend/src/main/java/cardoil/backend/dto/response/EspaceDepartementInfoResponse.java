package cardoil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class EspaceDepartementInfoResponse {
    private Long departementId;
    private String departementNom;
    private String entrepriseNom;
    private BigDecimal budgetDisponible;
    private int nombreEmployes;
    private int cartesActives;
    private int cartesSuspendues;
    private int cartesBloquees;
    private int cartesExpirees;
    private BigDecimal soldeTotalCartes;
}