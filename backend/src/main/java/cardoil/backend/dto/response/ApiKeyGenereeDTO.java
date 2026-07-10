package cardoil.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Retourné une seule fois après génération/rotation — la clé en clair n'est jamais restockée
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyGenereeDTO {
    private Long etablissementId;
    private String apiKey; // clé complète, affichée UNE fois
    private String apiKeyPrefix;
}