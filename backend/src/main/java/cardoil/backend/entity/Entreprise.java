package cardoil.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "entreprises")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Entreprise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(unique = true, nullable = false)
    private String code;

    private String secteurActivite;
    private String adresse;
    private String telephone;
    private String email;

    @Builder.Default
    private boolean actif = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compagnie_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Compagnie compagnie;

    private LocalDateTime dateCreation;

    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
    }

   
@Builder.Default
@Column(nullable = false, precision = 15, scale = 2)
private java.math.BigDecimal soldeDisponible = java.math.BigDecimal.ZERO;

}