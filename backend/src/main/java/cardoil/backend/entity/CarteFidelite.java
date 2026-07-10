package cardoil.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cartes_fidelite")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarteFidelite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String token; // Token QR

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeQr typeQr; // STATIQUE ou DYNAMIQUE

    @Column(length = 4)
    private String pin; // Hashé BCrypt (QR statique uniquement)

    @Builder.Default
    private BigDecimal solde = BigDecimal.ZERO;

    @Builder.Default
    private int pointsFidelite = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutCarte statut;

    @Builder.Default
    private int tentativesPin = 0;

    private LocalDateTime dateCreation;

    private LocalDateTime dateExpiration;

    // Propriétaire — client particulier ou employé
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compagnie_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Compagnie compagnie;

    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
    }
}