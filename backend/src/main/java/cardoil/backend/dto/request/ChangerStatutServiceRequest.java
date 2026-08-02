package cardoil.backend.dto.request;

import cardoil.backend.entity.StatutService;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangerStatutServiceRequest {
    @NotNull(message = "Le statut est obligatoire")
    private StatutService statut;
}