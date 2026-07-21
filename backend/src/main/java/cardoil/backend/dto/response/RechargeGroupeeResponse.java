package cardoil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class RechargeGroupeeResponse {
    private Long id;
    private String nomFichier;
    private LocalDateTime dateExecution;
    private String effectuePar;
    private int nombreReussies;
    private int nombreEchecs;
    private BigDecimal montantTotal;
    private String detailsErreurs;
}