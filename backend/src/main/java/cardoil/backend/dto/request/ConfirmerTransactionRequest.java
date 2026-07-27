package cardoil.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConfirmerTransactionRequest {
    @NotBlank
    private String code;
}