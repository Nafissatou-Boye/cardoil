package cardoil.backend.entity;

import cardoil.backend.enums.StatutCompteClient;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "client")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class Client extends Utilisateur {

    // telephone retiré : vit maintenant sur Utilisateur (champ partagé avec Employé),
    // évite la collision de nom parent/enfant qu'on a déjà rencontrée une fois avec "entreprise".

    @Column(unique = true, length = 50)
    private String nomUtilisateur;

    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal solde = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutCompteClient statutCompte = StatutCompteClient.ACTIF;

    @Column(length = 6)
    private String codeOtp;

    private LocalDateTime codeOtpExpiration;

    @Builder.Default
    @Column(nullable = false)
    private boolean telephoneVerifie = false;
}