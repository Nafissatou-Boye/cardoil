package cardoil.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaysRequest {

    @NotBlank(message = "Le code ISO est obligatoire")
    @Size(min = 2, max = 3, message = "Le code ISO doit contenir 2 ou 3 caractères")
    private String codeIso;

    @NotBlank(message = "Le nom du pays est obligatoire")
    private String nom;

    private String devise;

    private String indicatifTel;

    private boolean actif;

    
}