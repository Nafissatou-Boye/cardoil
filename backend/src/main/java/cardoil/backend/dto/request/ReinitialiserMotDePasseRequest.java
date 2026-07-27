package cardoil.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ReinitialiserMotDePasseRequest {

    @NotBlank
    private String telephone;

    @NotBlank
    private String code;

    @NotBlank(message = "Le nouveau mot de passe est obligatoire")
    @Pattern(regexp = "^\\d{4,6}$", message = "Le mot de passe doit contenir entre 4 et 6 chiffres")
    private String nouveauMotDePasse;
}