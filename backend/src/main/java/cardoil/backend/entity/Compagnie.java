package cardoil.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "compagnies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Compagnie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(unique = true, nullable = false)
    private String code; // Code interne unique

    private String logo;

    private String adresse;

    private String telephone;

    private String email;

    @Builder.Default
    private boolean actif = true;

    private LocalDateTime dateCreation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pays_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Pays pays;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Utilisateur admin; // Admin Compagnie

    @OneToMany(mappedBy = "compagnie", cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Station> stations;

    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
    }
}