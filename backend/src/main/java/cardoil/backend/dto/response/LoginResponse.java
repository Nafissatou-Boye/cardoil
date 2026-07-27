package cardoil.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String role;
    private String nom;
    private String prenom;
    private boolean doitChangerMotDePasse;
    // À ajouter dans LoginResponse.java, comme nouveaux champs :

private Long id;
private String telephone;
}