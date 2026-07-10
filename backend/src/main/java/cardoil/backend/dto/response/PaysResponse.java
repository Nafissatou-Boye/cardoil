package cardoil.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaysResponse {

    private Long id;
    private String codeIso;
    private String nom;
    private String devise;
    private String indicatifTel;
    private boolean actif;
    private long nombreCompagnies;
}