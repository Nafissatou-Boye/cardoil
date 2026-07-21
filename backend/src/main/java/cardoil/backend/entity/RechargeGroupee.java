package cardoil.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "recharges_groupees")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RechargeGroupee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomFichier;

    private LocalDateTime dateExecution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "effectue_par_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Utilisateur effectuePar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entreprise_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Entreprise entreprise;

    @Builder.Default
    private int nombreReussies = 0;

    @Builder.Default
    private int nombreEchecs = 0;

    @Builder.Default
    @Column(precision = 15, scale = 2)
    private BigDecimal montantTotal = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String detailsErreurs;

    @PrePersist
    public void prePersist() {
        this.dateExecution = LocalDateTime.now();
    }
}