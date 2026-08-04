package cardoil.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompagnieOptionResponse {
    private Long id;
    private String nom;
    private String indicatifPays;
}