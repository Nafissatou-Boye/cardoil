package cardoil.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

// Requête entrante du partenaire (POST /api/v1/recharge)
@Data
public class RechargeRequestDTO {

    @NotBlank(message = "Le numéro de téléphone est requis")
    @Pattern(regexp = "^\\+[1-9]\\d{6,14}$", message = "Format E.164 attendu, ex: +221771234567")
    private String phoneNumber;

    @NotNull(message = "Le montant est requis")
    @DecimalMin(value = "0.01", message = "Le montant doit être strictement positif")
    private BigDecimal amount;

    @NotBlank(message = "La référence de transaction est requise")
    @Size(max = 100)
    private String reference;

    @NotBlank(message = "L'identifiant compagnie est requis")
    private String companyId;

    @Size(max = 255)
    private String description;

    @NotNull(message = "Le timestamp est requis")
    private OffsetDateTime timestamp;
}