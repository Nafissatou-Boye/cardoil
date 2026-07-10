package cardoil.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RechargeStatsParPartenaireDTO {
    private Long etablissementId;
    private String etablissementNom;
    private long nombreTransactions;
    private long nombreReussies;
    private long nombreEchouees;
    private BigDecimal montantTotalRecharge;
    private double tauxEchec;
}