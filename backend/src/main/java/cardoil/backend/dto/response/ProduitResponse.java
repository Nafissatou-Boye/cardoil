package cardoil.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProduitResponse {

    private Long id;
    private String nom;
    private String type;
    private String statut;
    private String description;
    private boolean obligatoire;

    private String categorie; // 🆕
    private String unite; // 🆕
    private BigDecimal commissionFixe; // 🆕
    private BigDecimal commissionPourcentage; // 🆕

    // Prix actuellement en vigueur (dernier PrixJour enregistré)
    private BigDecimal prixTtcActuel;
    private BigDecimal prixHtvaActuel;
    private BigDecimal prixHttActuel;
    private String datePrixActuel;
}