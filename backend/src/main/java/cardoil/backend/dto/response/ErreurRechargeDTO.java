package cardoil.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Corps d'erreur structuré, conforme section 5 du CDC
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErreurRechargeDTO {
    private String errorCode;
    private String message;
    private String champ;
}