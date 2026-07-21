package cardoil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EmployeResponse {

    private Long id;
    private String login;
    private String matricule;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private boolean actif;
    private LocalDateTime dateCreation;

    private Long departementId;
    private String departementNom;

    private Long entrepriseId;
    private String entrepriseNom;

    private boolean possedeUneCarte;

    private String motDePasseTemporaire;
}