package cardoil.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompagnieProfilResponse {

    private Long id;
    private String nom;
    private String code;
    private String adresse;
    private String telephone;
    private String email;
    private String logo;
    private String paysNom;
    private String dateCreation;
}