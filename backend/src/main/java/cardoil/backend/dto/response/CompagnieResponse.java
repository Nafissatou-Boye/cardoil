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
public class CompagnieResponse {

    private Long id;
    private String nom;
    private String code;
    private String logo;
    private String adresse;
    private String telephone;
    private String email;
    private boolean actif;
    private LocalDateTime dateCreation;
    private PaysInfo pays;
    private long nombreStations;
    private String adminNom;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaysInfo {
        private Long id;
        private String nom;
        private String codeIso;
    }
}