package cardoil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResoudreQrResponse {
    private String porteurNom;
    private String typePorteur; // "CLIENT" ou "EMPLOYE"
}