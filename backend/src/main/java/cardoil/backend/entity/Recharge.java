package cardoil.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "recharges")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recharge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carte_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Carte carte;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montant;

    private LocalDateTime dateRecharge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "effectue_par_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Utilisateur effectuePar;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeRecharge type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recharge_groupee_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private RechargeGroupee rechargeGroupee;

    @PrePersist
    public void prePersist() {
        this.dateRecharge = LocalDateTime.now();
    }
}