package cardoil.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "utilisateurs")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String login;

    @Column(nullable = false)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String nom;

    private String prenom;

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

    // Utilisé uniquement pour les comptes ADMIN_DEPARTEMENT : le département
    // que cet utilisateur gère. Nommé différemment de Employe.departement
    // pour éviter toute collision de nom entre parent/enfant (cf. bug entreprise).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departement_gere_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Departement departementGere;

    
}