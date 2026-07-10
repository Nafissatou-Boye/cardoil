package cardoil.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardAdminCompagnieResponse {

    private String nomCompagnie;
    private String codeCompagnie;
    private long totalStations;
    private long stationsActives;
    private long totalEmployes;
}