package cardoil.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuperAdminProfilResponse {
    private Long id;
    private String login;
    private String nom;
    private String prenom;
    private String email;
    private String dateCreation;
}