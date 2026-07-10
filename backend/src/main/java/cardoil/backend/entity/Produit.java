package cardoil.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "produits")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom; // Ex: Essence, Gasoil, Lavage

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeProduit type; // LIQUIDE ou NON_LIQUIDE

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutProduit statut;

    @Column(length = 50)
    private String categorie; // 🆕 CDC section 9 — catégorie commerciale

    @Column(length = 20)
    private String unite; // 🆕 CDC section 9 — "Litre", "Unité", "Prestation"...

    @Column(precision = 10, scale = 2)
    private BigDecimal commissionFixe; // 🆕 CDC section 5 — FCFA/litre, utilisé si type = LIQUIDE

    @Column(precision = 5, scale = 2)
    private BigDecimal commissionPourcentage; // 🆕 CDC section 5 — %, utilisé si type = NON_LIQUIDE

    private String description;

    @Builder.Default
    private boolean obligatoire = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compagnie_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Compagnie compagnie;
}