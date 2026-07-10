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
public class StationResponse {

    private Long id;
    private String nom;
    private String adresse;
    private Double latitude;
    private Double longitude;
    private String telephone;
    private boolean actif;
    private LocalDateTime dateCreation;
    private CompagnieInfo compagnie;
    private String gerantNom;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompagnieInfo {
        private Long id;
        private String nom;
        private String code;
    }
}