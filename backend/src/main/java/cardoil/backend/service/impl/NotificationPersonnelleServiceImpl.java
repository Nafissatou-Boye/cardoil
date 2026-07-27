// NotificationPersonnelleServiceImpl.java
package cardoil.backend.service.impl;

import cardoil.backend.dto.response.NotificationPersonnelleResponse;
import cardoil.backend.entity.NotificationPersonnelle;
import cardoil.backend.entity.Utilisateur;
import cardoil.backend.repository.NotificationPersonnelleRepository;
import cardoil.backend.repository.UtilisateurRepository;
import cardoil.backend.service.NotificationPersonnelleService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationPersonnelleServiceImpl implements NotificationPersonnelleService {

    private final NotificationPersonnelleRepository notificationRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Override
    public List<NotificationPersonnelleResponse> mesNotifications(String login) {
        Utilisateur utilisateur = getUtilisateur(login);
        return notificationRepository.findByDestinataireIdOrderByDateCreationDesc(utilisateur.getId())
                .stream().map(this::toResponse).toList();
    }

    @Override
    public long compteNonLues(String login) {
        Utilisateur utilisateur = getUtilisateur(login);
        return notificationRepository.countByDestinataireIdAndLu(utilisateur.getId(), false);
    }

    @Override
    public void marquerLue(String login, Long id) {
        Utilisateur utilisateur = getUtilisateur(login);
        NotificationPersonnelle notif = notificationRepository.findByIdAndDestinataireId(id, utilisateur.getId())
                .orElseThrow(() -> new EntityNotFoundException("Notification non trouvée"));
        if (!notif.isLu()) {
            notif.setLu(true);
            notif.setDateLecture(LocalDateTime.now());
            notificationRepository.save(notif);
        }
    }

    @Override
    public void marquerToutLu(String login) {
        Utilisateur utilisateur = getUtilisateur(login);
        List<NotificationPersonnelle> nonLues = notificationRepository
                .findByDestinataireIdAndLu(utilisateur.getId(), false);
        LocalDateTime maintenant = LocalDateTime.now();
        nonLues.forEach(n -> { n.setLu(true); n.setDateLecture(maintenant); });
        notificationRepository.saveAll(nonLues);
    }

    @Override
    public void supprimer(String login, Long id) {
        Utilisateur utilisateur = getUtilisateur(login);
        NotificationPersonnelle notif = notificationRepository.findByIdAndDestinataireId(id, utilisateur.getId())
                .orElseThrow(() -> new EntityNotFoundException("Notification non trouvée"));
        notificationRepository.delete(notif);
    }

    private Utilisateur getUtilisateur(String login) {
        return utilisateurRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));
    }

    private NotificationPersonnelleResponse toResponse(NotificationPersonnelle n) {
        var builder = NotificationPersonnelleResponse.builder()
                .id(n.getId())
                .titre(n.getTitre())
                .message(n.getMessage())
                .type(n.getType().name())
                .lu(n.isLu())
                .dateCreation(n.getDateCreation())
                .dateLecture(n.getDateLecture());

        if (n.getTransaction() != null) {
            var tx = n.getTransaction();
            builder.transactionId(tx.getId())
                    .montant(tx.getMontant())
                    .stationNom(tx.getStation() != null ? tx.getStation().getNom() : null)
                    .produitNom(tx.getProduit() != null ? tx.getProduit().getNom()
                            : (tx.getService() != null ? tx.getService().getNom() : null));
        }
        return builder.build();
    }
}