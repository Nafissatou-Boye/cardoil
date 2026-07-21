package cardoil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminDepartementInfoResponse {
    private Long id;
    private String login;
    private String nom;
    private String prenom;
    private String email;
    private boolean actif;
    private String motDePasseTemporaire;
}