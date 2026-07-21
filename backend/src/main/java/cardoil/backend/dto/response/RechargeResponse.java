package cardoil.backend.dto.response;

import cardoil.backend.entity.TypeRecharge;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class RechargeResponse {
    private Long id;
    private String numeroCarte;
    private BigDecimal montant;
    private LocalDateTime dateRecharge;
    private String effectuePar;
    private TypeRecharge type;
}