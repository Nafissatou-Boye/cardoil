package cardoil.backend.dto.response;

import cardoil.backend.enums.StatutEtablissement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiaisonDTO {
    private Long id;
    private Long compagnieId;
    private String compagnieNom;
    private StatutEtablissement statut;
    private BigDecimal montantMinimum;
    private BigDecimal montantMaximumParTransaction;
    private BigDecimal plafondJournalierParClient;
    private LocalDateTime dateActivation;
    // ajouter ce champ, par exemple juste après compagnieNom
private String devise;
}