package cardoil.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompagnieOptionResponse {
    private Long id;
    private String nom;
}