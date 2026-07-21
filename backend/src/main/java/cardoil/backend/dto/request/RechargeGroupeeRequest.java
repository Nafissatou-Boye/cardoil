package cardoil.backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class RechargeGroupeeRequest {
    private String nomFichier;

    @NotEmpty(message = "Le fichier ne contient aucune ligne à traiter")
    @Valid
    private List<LigneRechargeRequest> lignes;
}