package cardoil.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepartementRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    private String description;

    @DecimalMin(value = "0.0", inclusive = true, message = "Le budget ne peut pas être négatif")
    private BigDecimal budget;

    private boolean actif = true;
}