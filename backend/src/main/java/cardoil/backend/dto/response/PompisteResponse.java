// PompisteResponse.java — dédié à "un Gérant consulte SES pompistes"
package cardoil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PompisteResponse {
    private Long id;
    private String nom;
    private String prenom;
    private String telephone;
    private String role;
    private boolean actif;
}