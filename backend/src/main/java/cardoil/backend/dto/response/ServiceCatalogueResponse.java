package cardoil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ServiceCatalogueResponse {
    private Long id;
    private String code;
    private String nom;
    private String categorie;
    private String description;
    private BigDecimal prix;
    private String icone;
    private String couleurHex;
    private String statut;
    private boolean obligatoire;
    private int ordreTri;
    private Long compagnieId;
    private String compagnieNom;
    // Vide = disponible dans toutes les stations de la compagnie.
    private List<Long> stationIds;
    private List<String> stationNoms;
}