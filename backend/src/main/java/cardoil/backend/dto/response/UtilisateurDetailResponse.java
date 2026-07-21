package cardoil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UtilisateurDetailResponse {
    private Long id;
    private String login;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String role;
    private boolean actif;
    private LocalDateTime dateCreation;
    private LocalDateTime derniereConnexion;

    // Employé uniquement
    private String matricule;
    private String departementNom;
    private CarteResponse carte;
    private List<RechargeResponse> historiqueRecharges;

    // Admin Département uniquement
    private String departementGereNom;
    private Integer nombreEmployesDepartementGere;
}