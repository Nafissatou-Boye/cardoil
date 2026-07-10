package cardoil.backend.dto.request;

import cardoil.backend.entity.TypeCadeau;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CadeauRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    private String nom;

    @NotNull(message = "Le type est obligatoire")
    private TypeCadeau type;

    @NotNull(message = "Le coût en points est obligatoire")
    @Min(value = 1, message = "Le coût doit être d'au moins 1 point")
    private Integer coutEnPoints;

    @NotNull(message = "Le stock est obligatoire")
    @Min(value = 0, message = "Le stock ne peut pas être négatif")
    private Integer stockDisponible;

    private String image;
    private String descriptionLongue;
    private LocalDate dateExpiration;
    private boolean actif;
}