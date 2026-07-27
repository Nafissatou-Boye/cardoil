// StatsJourResponse.java
package cardoil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class StatsJourResponse {
    private BigDecimal totalAmount;
    private long totalCount;
    private long successCount;
}