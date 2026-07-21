package cardoil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SuiviBudgetResponse {
    private BigDecimal soldeDisponibleEntreprise;
    private BigDecimal totalBudgetAlloueDepartements;
    private BigDecimal totalSoldeCartesActives;
}