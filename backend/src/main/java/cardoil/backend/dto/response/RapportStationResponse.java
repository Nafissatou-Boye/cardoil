package cardoil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RapportStationResponse {
    private String nom;
    private BigDecimal ca;
    private long nbTransactions;
}