package cardoil.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EntrepriseRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le code est obligatoire")
    private String code;

    private String secteurActivite;
    private String adresse;
    private String telephone;

    @Email(message = "Email invalide")
    private String email;

    private boolean actif = true;
}