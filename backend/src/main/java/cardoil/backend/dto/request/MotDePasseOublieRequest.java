package cardoil.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MotDePasseOublieRequest {

    @NotBlank(message = "Le téléphone est obligatoire")
    private String telephone;
}