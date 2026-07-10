package cardoil.backend.entity;

import cardoil.backend.enums.CodeErreurRecharge;
import cardoil.backend.enums.StatutRecharge;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "recharge_externe",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_partenaire_reference",
           columnNames = {"etablissement_financier_id", "reference_partenaire"}))
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RechargeExterne {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String referencePartenaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etablissement_financier_id", nullable = false)
    private EtablissementFinancier etablissementFinancier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compagnie_id", nullable = false)
    private Compagnie compagnie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @Column(nullable = false, length = 20)
    private String telephoneClient;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montant;

   @Column(nullable = false, length = 10)
private String devise;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutRecharge statut = StatutRecharge.PENDING;

    @Enumerated(EnumType.STRING)
    private CodeErreurRecharge codeErreur;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private LocalDateTime dateDemande;

    private LocalDateTime dateTraitement;

    @Column(length = 45)
    private String adresseIp;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime dateCreation = LocalDateTime.now();
}