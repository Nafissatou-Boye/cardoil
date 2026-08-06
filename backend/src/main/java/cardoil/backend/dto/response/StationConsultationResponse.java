package cardoil.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StationConsultationResponse {
    private Long id;
    private String nom;
    private String adresse;
    private Double latitude;
    private Double longitude;
    private String telephone;
}