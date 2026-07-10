package cardoil.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dateTransaction;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal montant; // Montant saisi par le pompiste

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeTransaction type; // ACHAT ou RECHARGE

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutTransaction statut; // REUSSIE, ECHEC, EN_ATTENTE

    // Prix informatifs au moment de la transaction
    @Column(precision = 12, scale = 2)
    private BigDecimal prixTtc;

    @Column(precision = 12, scale = 2)
    private BigDecimal prixHtva;

    @Column(precision = 12, scale = 2)
    private BigDecimal prixHtt;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Produit produit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Station station;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operateur_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Utilisateur operateur; // Pompiste ou Gérant

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Utilisateur client; // Client particulier ou Employé

    @PrePersist
    public void prePersist() {
        this.dateTransaction = LocalDateTime.now();
    }
}