package cardoil.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "utilisateurs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 7)
    private String login;

    @Column(nullable = false)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false)
    private String email;

    @Builder.Default
    @Column(nullable = false)
    private boolean actif = true;

    @Builder.Default
    private int tentativesEchouees = 0;

    @Builder.Default
    private boolean bloque = false;

    private LocalDateTime dateCreation;

    private LocalDateTime derniereConnexion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compagnie_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Compagnie compagnie;

    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
    }

    @Builder.Default
private boolean doitChangerMotDePasse = false;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "entreprise_id")
@ToString.Exclude
@EqualsAndHashCode.Exclude
private Entreprise entreprise;
}