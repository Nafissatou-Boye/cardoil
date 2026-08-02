// dto/FideliteCompteResponse.java
package cardoil.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FideliteCompteResponse {
    private Long id;
    private Long clientId;
    private Long companyId;
    private Integer points;
    private Integer totalEarned;
    private Integer totalRedeemed;
    private LocalDateTime lastTransactionDate;
}