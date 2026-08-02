package cardoil.backend.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PayerParQrRequest {
    private String code;
    private BigDecimal montant;
    private Long produitId;
    private Long serviceId;
}