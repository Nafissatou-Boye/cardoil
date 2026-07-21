package cardoil.backend.dto.response;

import cardoil.backend.entity.StatutCarte;
import cardoil.backend.entity.TypeCarteEmploye;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RapportEmployeResponse {
    private Long employeId;
    private String nomComplet;
    private String matricule;
    private String departementNom;
    private String numeroCarte;
    private TypeCarteEmploye typeCarte;
    private StatutCarte statutCarte;
    private BigDecimal soldeActuel;
    private BigDecimal totalCredite;
}