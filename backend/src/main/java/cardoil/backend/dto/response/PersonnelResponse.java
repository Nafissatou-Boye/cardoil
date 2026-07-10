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
public class PersonnelResponse {

    private Long id;
    private String login;
    private String nom;
    private String prenom;
    private String email;
    private String role;
    private boolean actif;
    private LocalDateTime dateCreation;
    private LocalDateTime derniereConnexion;
    private StationInfo station;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StationInfo {
        private Long id;
        private String nom;
    }

    private String motDePasseTemporaire; // Affiché une seule fois à la création
}