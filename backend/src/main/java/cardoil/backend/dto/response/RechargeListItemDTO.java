package cardoil.backend.dto.response;

import cardoil.backend.enums.CodeErreurRecharge;
import cardoil.backend.enums.StatutRecharge;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RechargeListItemDTO {
    private UUID id;
    private String referencePartenaire;
    private String etablissementNom;
    private String compagnieNom;
    private String telephoneClient;
    private BigDecimal montant;
    private String devise;
    private StatutRecharge statut;
    private CodeErreurRecharge codeErreur;
    private LocalDateTime dateDemande;
    private LocalDateTime dateTraitement;
}