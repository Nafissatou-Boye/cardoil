// ServiceCatalogue.java
package cardoil.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "services_catalogue")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceCatalogue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String code;

    @Column(nullable = false)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategorieService categorie;

    private String description;

    @Column(precision = 12, scale = 0)
    private BigDecimal prix;

    private String icone;
    private String couleurHex;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutService statut;

    @Builder.Default
    private boolean obligatoire = false;

    @Builder.Default
    private int ordreTri = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compagnie_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Compagnie compagnie;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "service_stations",
        joinColumns = @JoinColumn(name = "service_id"),
        inverseJoinColumns = @JoinColumn(name = "station_id")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Station> stationsDisponibles;
}