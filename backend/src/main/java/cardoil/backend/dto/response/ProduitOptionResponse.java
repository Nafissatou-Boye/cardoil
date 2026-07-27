// ProduitOptionResponse.java
package cardoil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProduitOptionResponse {
    private Long id;
    private String nom;
}