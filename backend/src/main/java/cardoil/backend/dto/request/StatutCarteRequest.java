package cardoil.backend.dto.request;

import cardoil.backend.entity.StatutCarte;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatutCarteRequest {
    @NotNull(message = "Le statut est obligatoire")
    private StatutCarte statut;
}