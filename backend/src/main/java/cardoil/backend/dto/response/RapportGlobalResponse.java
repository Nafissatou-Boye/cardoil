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
public class RapportGlobalResponse {

  private BigDecimal caTotal;
    private long totalTransactions;
    private long transactionsReussies;
    private long transactionsEchec;
    private List<RapportStationResponse> parStation;
    private List<RapportTransactionResponse> dernieresTransactions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StationRapport {
        private Long id;
        private String nom;
        private BigDecimal ca;
        private long nbTransactions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionRapport {
        private Long id;
        private String dateTransaction;
        private BigDecimal montant;
        private String type;
        private String statut;
        private String station;
        private String operateur;
    }
}