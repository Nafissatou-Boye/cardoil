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
public class RechargeStatsDTO {
    private long nombreTransactions;
    private long nombreReussies;
    private long nombreEchouees;
    private long nombreEnAttente;
    private BigDecimal montantTotalRecharge; // somme des montants, transactions SUCCESS uniquement
    private double tauxEchec; // en %, calculé sur (reussies + echouees) — PENDING/COMPENSATED exclus du calcul
    private Double tempsTraitementMoyenMs; // null si aucune transaction traitée dans la période
}