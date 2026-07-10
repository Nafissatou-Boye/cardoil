package cardoil.backend.entity;

import cardoil.backend.enums.StatutEtablissement;
import cardoil.backend.enums.TypeEtablissement;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "etablissement_financier")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtablissementFinancier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nom;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeEtablissement type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutEtablissement statut = StatutEtablissement.ACTIF;

    @Column(nullable = false, unique = true)
    private String apiKeyHash;

    @Column(nullable = false, length = 12)
    private String apiKeyPrefix;

    @Column
    private LocalDateTime apiKeyExpiration;

    @Column(length = 500)
    private String ipWhitelist;

    @Column(nullable = false)
    @Builder.Default
    private Integer rateLimitParMinute = 60;

    @Column(length = 150)
    private String emailContact;

    @Column(length = 20)
    private String telephoneContact;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime dateCreation = LocalDateTime.now();

    private LocalDateTime dateDerniereRotationCle;

    @OneToMany(mappedBy = "etablissementFinancier", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EtablissementFinancierCompagnie> liaisonsCompagnies = new ArrayList<>();
}