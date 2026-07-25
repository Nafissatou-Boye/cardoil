package cardoil.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class InscriptionClientRequest {

    @NotBlank(message = "Le nom complet est obligatoire")
    private String nomComplet;

    @NotBlank(message = "Le téléphone est obligatoire")
    private String telephone;

    @NotNull(message = "La compagnie est obligatoire")
    private Long compagnieId;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Pattern(regexp = "^\\d{4,6}$", message = "Le mot de passe doit contenir entre 4 et 6 chiffres")
    private String motDePasse;

    @NotBlank(message = "La confirmation du mot de passe est obligatoire")
    private String confirmerMotDePasse;
}