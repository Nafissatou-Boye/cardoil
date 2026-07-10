package cardoil.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "promotions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Étape 1 — Identité
    @Column(nullable = false)
    private String nom;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypePromotion type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutPromotion statut;

    // Étape 2 — Période
    @Column(nullable = false)
    private LocalDateTime dateDebut;

    @Column(nullable = false)
    private LocalDateTime dateFin;

    // Étape 3 — Éligibilité
    private BigDecimal montantMinimum;

    // Stations concernées (null = toutes les stations)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "promotion_stations",
        joinColumns = @JoinColumn(name = "promotion_id"),
        inverseJoinColumns = @JoinColumn(name = "station_id")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Station> stationsConcernees;

    // Étape 4 — Limites
    private Integer plafondParClient;
    private Integer plafondGlobal;
    private Integer plafondJournalier;

    // Étape 5 — Récompenses POINTS
    private Integer pointsParTranche;
    private BigDecimal montantParTranche;

    // Étape 5 — Récompenses GIFT
    private String descriptionCadeau;
    private Integer stockCadeaux;

    // Étape 5 — Récompenses SCRATCH
    private BigDecimal probabiliteGain;
    private String descriptionLot;

    @Builder.Default
    private boolean actif = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compagnie_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Compagnie compagnie;

    @PrePersist
    public void prePersist() {
        if (this.statut == null) this.statut = StatutPromotion.DRAFT;
    }
}