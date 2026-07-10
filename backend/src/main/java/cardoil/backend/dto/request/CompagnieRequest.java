package cardoil.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompagnieRequest {

    @NotBlank(message = "Le nom de la compagnie est obligatoire")
    private String nom;

    @NotBlank(message = "Le code est obligatoire")
    private String code;

    private String logo;

    private String adresse;

    private String telephone;

    @Email(message = "Email invalide")
    private String email;

    private boolean actif;

    @NotNull(message = "Le pays est obligatoire")
    private Long paysId;
}