package cardoil.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PrixProduitRequestDTO {
    private BigDecimal prixTtc;
    private BigDecimal prixHtva;
    private BigDecimal prixHtt;

    @NotNull(message = "La date de début est requise")
    private LocalDate dateDebut;

    private LocalDate dateFin; // optionnel — NULL = indéterminée
}