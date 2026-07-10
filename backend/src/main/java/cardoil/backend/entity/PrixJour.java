package cardoil.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
    name = "prix_jour",
    uniqueConstraints = @UniqueConstraint(columnNames = {"produit_id", "date_prix"})
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrixJour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Produit produit;

    @Column(precision = 12, scale = 2)
    private BigDecimal prixTtc;   // NULL si non défini

    @Column(precision = 12, scale = 2)
    private BigDecimal prixHtva;  // NULL si non défini

    @Column(precision = 12, scale = 2)
    private BigDecimal prixHtt;   // NULL si non défini

    @Column(nullable = false)
    private LocalDate datePrix;
}