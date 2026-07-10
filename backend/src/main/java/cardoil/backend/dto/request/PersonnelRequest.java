package cardoil.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import cardoil.backend.entity.Role;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonnelRequest {

    // Plus obligatoire — auto-généré si absent
    private String login;

    // Optionnel à la modification
    private String motDePasse;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Email invalide")
    private String email;

    private boolean actif;
    private Long stationId;
    
  @Builder.Default
  private Role role = Role.GERANT;
}