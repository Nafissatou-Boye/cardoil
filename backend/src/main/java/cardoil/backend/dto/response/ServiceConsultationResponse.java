package cardoil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ServiceConsultationResponse {
    private Long id;
    private String code;
    private String name;
    private String categoryName;
    private String description;
    private BigDecimal prix;
    private String iconUrl;
    private String colorHex;
    private String status;
    private boolean mandatory;
    private int defaultDisplayOrder;
    private Long companyId;
}