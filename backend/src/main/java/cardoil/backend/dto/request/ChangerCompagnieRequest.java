package cardoil.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangerCompagnieRequest {

    @NotNull(message = "La compagnie est obligatoire")
    private Long compagnieId;
}