package cardoil.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifierCodeResetRequest {

    @NotBlank
    private String telephone;

    @NotBlank
    private String code;
}