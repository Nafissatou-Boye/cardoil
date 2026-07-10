package cardoil.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopCompanyResponse {

    private Long id;
    private String nom;
    private String code;
    private String paysNom;
    private int nombreStations;
    private boolean actif;
}