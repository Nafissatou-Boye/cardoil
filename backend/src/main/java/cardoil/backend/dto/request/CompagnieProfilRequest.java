package cardoil.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CompagnieProfilRequest {

    @NotBlank(message = "L'adresse est obligatoire")
    private String adresse;

    private String telephone;

    @Email(message = "Email invalide")
    private String email;

    private String logo;
}