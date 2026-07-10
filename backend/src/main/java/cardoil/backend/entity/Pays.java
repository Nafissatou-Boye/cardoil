package cardoil.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "pays")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pays {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 3)
    private String codeIso; // Ex: SN, FR, MA

    @Column(nullable = false)
    private String nom;

    private String devise; // Ex: FCFA, EUR

    private String indicatifTel; // Ex: +221, +33

    @Builder.Default
    private boolean actif = true;

    @OneToMany(mappedBy = "pays", cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Compagnie> compagnies;
}