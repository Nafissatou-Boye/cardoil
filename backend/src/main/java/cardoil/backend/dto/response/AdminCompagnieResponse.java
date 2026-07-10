package cardoil.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCompagnieResponse {

    private Long id;
    private String login;
    private String nom;
    private String prenom;
    private String email;
    private boolean actif;
    private String compagnieNom;
    private Long compagnieId;
    private LocalDateTime dateCreation;
    private LocalDateTime derniereConnexion;
}