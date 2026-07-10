package cardoil.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CadeauResponse {

    private Long id;
    private String nom;
    private String type;
    private int coutEnPoints;
    private int stockDisponible;
    private String image;
    private String descriptionLongue;
    private String dateExpiration;
    private boolean actif;
    private boolean illimite;
}