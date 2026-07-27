package cardoil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ClientProfilResponse {
    private Long id;
    private String nom;
    private String prenom;
    private String telephone;
    private String nomUtilisateur;
    private BigDecimal solde;
    private boolean telephoneVerifie;
    private boolean actif;

    private Long compagnieId;
    private String compagnieNom;

    private int pointsFideliteDisponibles;
    private int pointsFideliteTotal;
}