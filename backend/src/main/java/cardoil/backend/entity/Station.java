package cardoil.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "stations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Station {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String adresse;

    // Géolocalisation optionnelle
    private Double latitude;

    private Double longitude;

    private String telephone;

    @Builder.Default
    private boolean actif = true;

    private LocalDateTime dateCreation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compagnie_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Compagnie compagnie;

    // Un gérant unique par station
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gerant_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Utilisateur gerant;

    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
    }
}