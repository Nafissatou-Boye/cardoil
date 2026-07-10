package cardoil.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "entretiens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Entretien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String typeService; // Ex: Vidange, Révision, Lavage

    private String description;

    @Column(nullable = false)
    private LocalDateTime dateIntervention;

    private Integer kilometrageIntervention;

    @Column(precision = 12, scale = 2)
    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutEntretien statut;

    private LocalDateTime dateCreation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicule_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Vehicule vehicule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "garage_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Garage garage;

    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
    }
}