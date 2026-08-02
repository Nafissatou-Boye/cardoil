package cardoil.backend.dto.request;
import cardoil.backend.entity.CategorieService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ServiceCatalogueRequest {

    @NotBlank(message = "Le code est obligatoire")
    private String code;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotNull(message = "La catégorie est obligatoire")
    private CategorieService categorie;

    private String description;

    @PositiveOrZero(message = "Le prix ne peut pas être négatif")
    private BigDecimal prix;

    private String icone;
    private String couleurHex;

    private boolean obligatoire = false;
    private int ordreTri = 0;
}