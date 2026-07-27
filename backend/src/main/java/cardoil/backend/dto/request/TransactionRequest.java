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

    // Un seul des trois doit être renseigné, selon le scan effectué
    private String numeroCarte;  // Employé
    private String telephoneClient;  // Client particulier (identification manuelle/téléphone)
    private String qrCode;  // Client particulier (scan du QR rotatif, prioritaire si présent)
}