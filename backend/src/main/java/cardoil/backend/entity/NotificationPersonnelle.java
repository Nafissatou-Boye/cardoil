// NotificationPersonnelle.java
package cardoil.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications_personnelles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPersonnelle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinataire_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Utilisateur destinataire;

    @Column(nullable = false)
    private String titre;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeNotificationPersonnelle type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Transaction transaction;

    @Builder.Default
    @Column(nullable = false)
    private boolean lu = false;

    private LocalDateTime dateCreation;
    private LocalDateTime dateLecture;

    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
    }
}