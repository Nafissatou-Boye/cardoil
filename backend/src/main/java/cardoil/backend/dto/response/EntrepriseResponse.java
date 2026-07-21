package cardoil.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntrepriseResponse {

    private Long id;
    private String nom;
    private String code;
    private String secteurActivite;
    private String adresse;
    private String telephone;
    private String email;
    private boolean actif;
    private String dateCreation;
    private String compagnieNom;

private java.math.BigDecimal soldeDisponible;
}