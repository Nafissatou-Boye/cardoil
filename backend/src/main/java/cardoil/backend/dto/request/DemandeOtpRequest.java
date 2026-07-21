package cardoil.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DemandeOtpRequest {
    @NotBlank
    private String telephone;
}