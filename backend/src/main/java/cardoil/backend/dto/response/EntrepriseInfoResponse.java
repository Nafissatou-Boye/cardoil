package cardoil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class EntrepriseInfoResponse {
    private Long id;
    private String nom;
    private BigDecimal soldeDisponible;
}