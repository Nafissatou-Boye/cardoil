package cardoil.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "employes")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class Employe extends Utilisateur {

    @Column(nullable = false, unique = true)
    private String matricule;

    // Optionnel : présent seulement si l'employé est rattaché à un département précis
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departement_id", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Departement departement;

    // typeCarte, solde et plafondMensuel ont été retirés :
    // ils vivent maintenant sur l'entité Carte (relation OneToOne employe <-> carte),
    // conformément au modèle de données du cahier des charges (section 4).
}