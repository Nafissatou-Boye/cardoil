package cardoil.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cartes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Carte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String numeroCarte;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false, unique = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Employe employe;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeCarteEmploye typeCarte;

    @Builder.Default
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal solde = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private StatutCarte statut = StatutCarte.ACTIVE;

    private LocalDateTime dateCreation;

    private LocalDate dateExpiration;

    // Obligatoire pour DOTATION_PLAFONNEE et DOTATION_AVEC_REPORT
    @Column(precision = 15, scale = 2)
    private BigDecimal montantDotationMensuelle;

    // Jour du mois (1-28) pour le renouvellement automatique
    private Integer dateRenouvellement;

    // Optionnel, uniquement pour DOTATION_AVEC_REPORT
    @Column(precision = 15, scale = 2)
    private BigDecimal plafondCumuleMax;

    // Empêche une double application du renouvellement le même jour
    private LocalDate derniereDateRenouvellement;

    // ── QR dynamique (identité employé, scanné par le pompiste/gérant) ────
    // Miroir de Client.qrCode / Client.qrCodeExpiration. Utilisé par
    // EmployeCompteServiceImpl.genererQrCode() et
    // TransactionServiceImpl.payerParQr(). Null après un paiement réussi
    // (usage unique) ou avant toute génération.
    private String codeQr;
    private LocalDateTime codeQrExpiration;

    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
    }
}