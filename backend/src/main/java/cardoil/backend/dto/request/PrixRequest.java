package cardoil.backend.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PrixRequest {
    private BigDecimal prixTtc;
    private BigDecimal prixHtva;
    private BigDecimal prixHtt;

    private LocalDate dateDebut; // 🆕 optionnel — absent = aujourd'hui
    private LocalDate dateFin;   // 🆕 optionnel — absent = indéterminée
}