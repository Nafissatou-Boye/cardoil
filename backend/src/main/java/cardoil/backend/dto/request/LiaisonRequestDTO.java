package cardoil.backend.dto.request;

import lombok.Data;

import java.math.BigDecimal;

// Utilisé pour la création (POST) ET la modification (PATCH) d'une liaison établissement ↔ compagnie.
// compagnieId n'a de sens qu'à la création ; ignoré volontairement sur une modification.
@Data
public class LiaisonRequestDTO {
    private Long compagnieId;
    private BigDecimal montantMinimum;
    private BigDecimal montantMaximumParTransaction;
    private BigDecimal plafondJournalierParClient;
}