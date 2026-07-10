package cardoil.backend.dto.response;

import cardoil.backend.enums.StatutEtablissement;
import cardoil.backend.enums.TypeEtablissement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EtablissementFinancierDTO {
    private Long id;
    private String nom;
    private String code;
    private TypeEtablissement type;
    private StatutEtablissement statut;
    private String apiKeyPrefix; // jamais la clé complète
    private LocalDateTime apiKeyExpiration;
    private Integer rateLimitParMinute;
    private String emailContact;
    private LocalDateTime dateCreation;
    private int nombreCompagniesLiees;
}