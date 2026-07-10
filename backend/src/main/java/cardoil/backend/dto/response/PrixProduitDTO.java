package cardoil.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrixProduitDTO {
    private Long id;
    private Long produitId;
    private String produitNom;
    private BigDecimal prixTtc;
    private BigDecimal prixHtva;
    private BigDecimal prixHtt;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private boolean enVigueurAujourdHui; // calculé à la volée, pas stocké — pratique pour l'affichage Angular
}