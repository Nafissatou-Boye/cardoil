package cardoil.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminEntrepriseInfoResponse {
    private Long id;
    private String login;
    private String nom;
    private String prenom;
    private String email;
    private boolean actif;
}