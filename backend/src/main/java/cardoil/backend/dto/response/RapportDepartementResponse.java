package cardoil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RapportDepartementResponse {
    private Long departementId;
    private String departementNom;
    private BigDecimal budgetDisponible;
    private int nombreEmployes;
    private BigDecimal soldeCumuleCartes;
}