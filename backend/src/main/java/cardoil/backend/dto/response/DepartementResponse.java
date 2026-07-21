package cardoil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DepartementResponse {
    private Long id;
    private String nom;
    private String description;
    private BigDecimal budget;
    private boolean actif;
    private int nombreEmployes;
}