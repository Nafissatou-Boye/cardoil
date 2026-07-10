package cardoil.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GerantDashboardResponse {

    private Long stationId;
    private String stationNom;
    private String stationAdresse;

    private BigDecimal caJour;
    private long nbTransactionsJour;
    private long transactionsReussiesJour;
    private long transactionsEchecJour;

    private List<TransactionResponse> dernieresTransactions;
}