package cardoil.backend.entity;


import cardoil.backend.enums.StatutEtablissement;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Liaison Établissement Financier <-> Compagnie, avec statut et plafonds propres à la compagnie
@Entity
@Table(name = "etablissement_financier_compagnie",
       uniqueConstraints = @UniqueConstraint(columnNames = {"etablissement_financier_id", "compagnie_id"}))
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtablissementFinancierCompagnie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etablissement_financier_id", nullable = false)
    private EtablissementFinancier etablissementFinancier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compagnie_id", nullable = false)
    private Compagnie compagnie;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutEtablissement statut = StatutEtablissement.ACTIF; // activable/désactivable par la Compagnie

    @Builder.Default
    private BigDecimal montantMinimum = new BigDecimal("100");

    @Builder.Default
    private BigDecimal montantMaximumParTransaction = new BigDecimal("1000000");

    @Builder.Default
    private BigDecimal plafondJournalierParClient = new BigDecimal("2000000");

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime dateActivation = LocalDateTime.now();
}