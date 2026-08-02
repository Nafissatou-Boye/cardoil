package cardoil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PublicStatsResponse {
    private long totalCompagnies;
    private long totalStations;
    private BigDecimal volumeTraite;
}