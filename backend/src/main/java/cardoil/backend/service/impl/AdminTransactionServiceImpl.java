package cardoil.backend.service.impl;

import cardoil.backend.dto.response.RapportTransactionResponse;
import cardoil.backend.entity.Compagnie;
import cardoil.backend.entity.Transaction;
import cardoil.backend.entity.Utilisateur;
import cardoil.backend.repository.TransactionRepository;
import cardoil.backend.repository.UtilisateurRepository;
import cardoil.backend.service.AdminTransactionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminTransactionServiceImpl implements AdminTransactionService {

    private final UtilisateurRepository utilisateurRepository;
    private final TransactionRepository transactionRepository;

    // Même garde que les autres services Admin Compagnie de cette session.
    private Compagnie resolveCompagnie(String login) {
        Utilisateur utilisateur = utilisateurRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));
        if (utilisateur.getCompagnie() == null) {
            throw new IllegalStateException("Aucune compagnie associée à cet utilisateur");
        }
        return utilisateur.getCompagnie();
    }

    // Même logique que AdminCompagnieRapportServiceImpl.resolveDebut —
    // dupliquée plutôt que partagée, cohérent avec le reste de cette
    // session (chaque service résout sa propre compagnie indépendamment).
    private LocalDateTime resolveDebut(String periode) {
        LocalDateTime maintenant = LocalDateTime.now();
        return switch (periode) {
            case "7D" -> maintenant.minusDays(7);
            case "3M" -> maintenant.minusMonths(3);
            case "12M" -> maintenant.minusMonths(12);
            default -> maintenant.minusDays(30);
        };
    }

    @Override
    public List<RapportTransactionResponse> getTransactions(String login, String periode, Long stationId) {
        Compagnie compagnie = resolveCompagnie(login);
        LocalDateTime debut = resolveDebut(periode);
        LocalDateTime fin = LocalDateTime.now();

        return transactionRepository
                .findByStation_Compagnie_IdAndDateTransactionBetweenOrderByDateTransactionDesc(
                        compagnie.getId(), debut, fin)
                .stream()
                // Filtre par station en mémoire — évite une nouvelle méthode
                // de repository combinant compagnie + station + période,
                // jamais confirmée existante.
                .filter(t -> stationId == null
                        || (t.getStation() != null && t.getStation().getId().equals(stationId)))
                .map(this::toResponse)
                .toList();
    }

    private RapportTransactionResponse toResponse(Transaction t) {
        return RapportTransactionResponse.builder()
                .dateTransaction(t.getDateTransaction() != null ? t.getDateTransaction().toString() : null)
                .type(t.getType() != null ? t.getType().name() : null)
                .montant(t.getMontant())
                .station(t.getStation() != null ? t.getStation().getNom() : null)
                .operateur(t.getOperateur() != null
                        ? t.getOperateur().getPrenom() + " " + t.getOperateur().getNom()
                        : null)
                .statut(t.getStatut() != null ? t.getStatut().name() : null)
                .build();
    }
}