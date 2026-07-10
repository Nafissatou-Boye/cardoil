package cardoil.backend.dto.request;

import cardoil.backend.entity.TypeProduit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProduitRequest {

    @NotBlank(message = "Le nom du produit est obligatoire")
    private String nom;

    @NotNull(message = "Le type de produit est obligatoire")
    private TypeProduit type;

    private String description;

    private boolean obligatoire;

    private String categorie; // 🆕
    private String unite; // 🆕
    private BigDecimal commissionFixe; // 🆕 utilisé si type = LIQUIDE
    private BigDecimal commissionPourcentage; // 🆕 utilisé si type = NON_LIQUIDE
}