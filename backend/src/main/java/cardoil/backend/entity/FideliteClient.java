package cardoil.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "fidelite_clients",
       uniqueConstraints = @UniqueConstraint(columnNames = {"client_id", "compagnie_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FideliteClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Utilisateur client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compagnie_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Compagnie compagnie;

    @Builder.Default
    private int pointsTotal = 0;

    @Builder.Default
    private int pointsDisponibles = 0;

    @Builder.Default
    private int pointsUtilises = 0;

    private LocalDateTime derniereMaj;

    @PrePersist
    @PreUpdate
    public void preMaj() {
        this.derniereMaj = LocalDateTime.now();
    }
}