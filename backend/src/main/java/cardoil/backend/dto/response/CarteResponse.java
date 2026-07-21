package cardoil.backend.dto.response;

import cardoil.backend.entity.StatutCarte;
import cardoil.backend.entity.TypeCarteEmploye;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class CarteResponse {
    private Long id;
    private String numeroCarte;
    private Long employeId;
    private String employeNomComplet;
    private String matricule;
    private TypeCarteEmploye typeCarte;
    private BigDecimal solde;
    private StatutCarte statut;
    private LocalDateTime dateCreation;
    private LocalDate dateExpiration;
    private BigDecimal montantDotationMensuelle;
    private Integer dateRenouvellement;
    private BigDecimal plafondCumuleMax;
    private String sourceFinancement;
}