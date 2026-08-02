package cardoil.backend.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RechargeParQrRequest {
    private String code;
    private BigDecimal montant;
}