package cardoil.backend.dto.request;

import cardoil.backend.enums.TypeEtablissement;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EtablissementFinancierCreateDTO {
    @NotBlank
    private String nom;
    @NotBlank
    private String code;
    @NotNull
    private TypeEtablissement type;
    @Email
    private String emailContact;
    private String telephoneContact;
    private Integer rateLimitParMinute; // optionnel, défaut 60
}