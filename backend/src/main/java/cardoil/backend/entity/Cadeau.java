package cardoil.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cadeaux")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cadeau {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeCadeau type;

    @Column(nullable = false)
    private int coutEnPoints;

    @Column(nullable = false)
    private int stockDisponible; // 0 = illimité

    private String image;

    @Column(columnDefinition = "TEXT")
    private String descriptionLongue;

    private LocalDate dateExpiration;

    @Builder.Default
    private boolean actif = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compagnie_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Compagnie compagnie;

    private LocalDateTime dateCreation;

    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
    }
}