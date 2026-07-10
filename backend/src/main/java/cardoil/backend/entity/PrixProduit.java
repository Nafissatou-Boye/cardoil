package cardoil.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

// Prix CONFIGURÉ par l'Admin Compagnie, avec période de validité — distinct de PrixJour
// (qui est l'instantané journalier immuable généré automatiquement à partir de cette table).
@Entity
@Table(name = "prix_produit")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrixProduit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Produit produit;

    @Column(precision = 12, scale = 2)
    private BigDecimal prixTtc;

    @Column(precision = 12, scale = 2)
    private BigDecimal prixHtva;

    @Column(precision = 12, scale = 2)
    private BigDecimal prixHtt;

    @Column(nullable = false)
    private LocalDate dateDebut;

    @Column
    private LocalDate dateFin; // NULL = indéterminée (CDC section 6.2)
}