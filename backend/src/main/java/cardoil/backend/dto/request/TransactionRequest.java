package cardoil.backend.dto.request;

import cardoil.backend.entity.TypeTransaction;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

    @NotNull(message = "Le type de transaction est obligatoire")
    private TypeTransaction type;

    private Long produitId;

    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit être positif")
    private BigDecimal montant;

    // Un seul des deux doit être renseigné, selon qui se présente à la pompe
    private String numeroCarte;      // Employé
    private String telephoneClient;  // Client particulier
}