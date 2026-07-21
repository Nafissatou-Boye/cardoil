package cardoil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UtilisateurListItemResponse {
    private Long id;
    private String login;
    private String nom;
    private String prenom;
    private String email;
    private String role;
    private boolean actif;
    private String departementNom;
    private String matricule;
    private boolean possedeUneCarte;
}