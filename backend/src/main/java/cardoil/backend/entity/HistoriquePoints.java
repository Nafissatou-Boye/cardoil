package cardoil.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "historique_points")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoriquePoints {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fidelite_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private FideliteClient fideliteClient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeMouvementPoints type; // GAIN, UTILISATION, EXPIRATION

    @Column(nullable = false)
    private int points;

    private BigDecimal montantTransaction;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Transaction transaction;

    @Column(nullable = false)
    private LocalDateTime dateOperation;

    @PrePersist
    public void prePersist() {
        this.dateOperation = LocalDateTime.now();
    }
}