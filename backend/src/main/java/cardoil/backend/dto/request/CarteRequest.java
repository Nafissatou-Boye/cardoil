package cardoil.backend.dto.request;

import cardoil.backend.entity.TypeCarteEmploye;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CarteRequest {

    @NotNull(message = "L'employé est obligatoire")
    private Long employeId;

    @NotNull(message = "Le type de carte est obligatoire")
    private TypeCarteEmploye typeCarte;

    private LocalDate dateExpiration;

    // Obligatoire pour DOTATION_PLAFONNEE et DOTATION_AVEC_REPORT
    @DecimalMin(value = "1.0", message = "Le montant de dotation doit être d'au moins 1 FCFA")
    private BigDecimal montantDotationMensuelle;

    @Min(value = 1, message = "Le jour de renouvellement doit être entre 1 et 28")
    @Max(value = 28, message = "Le jour de renouvellement doit être entre 1 et 28")
    private Integer dateRenouvellement;

    // Optionnel, uniquement pour DOTATION_AVEC_REPORT
    private BigDecimal plafondCumuleMax;
}