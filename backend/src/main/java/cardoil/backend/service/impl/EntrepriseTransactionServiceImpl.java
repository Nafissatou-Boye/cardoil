package cardoil.backend.service.impl;

import cardoil.backend.dto.response.RapportTransactionEmployeResponse;
import cardoil.backend.entity.Entreprise;
import cardoil.backend.entity.Transaction;
import cardoil.backend.entity.Utilisateur;
import cardoil.backend.repository.TransactionRepository;
import cardoil.backend.repository.UtilisateurRepository;
import cardoil.backend.service.EntrepriseTransactionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class EntrepriseTransactionServiceImpl implements EntrepriseTransactionService {

    private final UtilisateurRepository utilisateurRepository;
    private final TransactionRepository transactionRepository;

    private Entreprise resolveEntreprise(String login) {
        Utilisateur utilisateur = utilisateurRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));
        if (utilisateur.getEntreprise() == null) {
            throw new IllegalStateException("Aucune entreprise associée à cet utilisateur");
        }
        return utilisateur.getEntreprise();
    }

    // Même logique que AdminTransactionServiceImpl.resolveDebut — dupliquée
    // plutôt que partagée, cohérent avec le reste de cette session.
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
    public List<RapportTransactionEmployeResponse> getTransactions(String login, String periode) {
        Entreprise entreprise = resolveEntreprise(login);
        LocalDateTime debut = resolveDebut(periode);
        LocalDateTime fin = LocalDateTime.now();

        return transactionRepository
                .findByClient_Entreprise_IdAndDateTransactionBetweenOrderByDateTransactionDesc(
                        entreprise.getId(), debut, fin)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private RapportTransactionEmployeResponse toResponse(Transaction t) {
        return RapportTransactionEmployeResponse.builder()
                .dateTransaction(t.getDateTransaction() != null ? t.getDateTransaction().toString() : null)
                .type(t.getType() != null ? t.getType().name() : null)
                .montant(t.getMontant())
                .employeNom(t.getClient() != null
                        ? t.getClient().getPrenom() + " " + t.getClient().getNom()
                        : null)
                .station(t.getStation() != null ? t.getStation().getNom() : null)
                .statut(t.getStatut() != null ? t.getStatut().name() : null)
                .build();
    }
}