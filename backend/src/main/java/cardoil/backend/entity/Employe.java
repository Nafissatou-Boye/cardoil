package cardoil.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "employes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String matricule;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 7)
    private String login; // 7 chiffres générés

    @Column(nullable = false, length = 4)
    private String motDePasse; // 4 chiffres générés

    @Builder.Default
    private BigDecimal solde = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal plafondMensuel = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeCarteEmploye typeCarte;

    @Builder.Default
    private boolean actif = true;

    private LocalDateTime dateCreation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departement_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Departement departement;

    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
    }
}