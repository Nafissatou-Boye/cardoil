// InitierTransactionRequest.java
package cardoil.backend.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InitierTransactionRequest {
    @NotNull @Positive
    private BigDecimal montant;

    private Long produitId;
    private Long serviceId;

    @AssertTrue(message = "Exactement un produit OU un service doit être précisé")
    private boolean isExactlyOneItemSet() {
        return (produitId != null) != (serviceId != null);
    }
}