package cardoil.backend.service.impl;

import cardoil.backend.dto.response.RapportGlobalResponse;
import cardoil.backend.dto.response.RapportStationResponse;
import cardoil.backend.dto.response.RapportTransactionResponse;
import cardoil.backend.entity.Compagnie;
import cardoil.backend.entity.Station;
import cardoil.backend.entity.StatutTransaction;
import cardoil.backend.entity.Transaction;
import cardoil.backend.entity.Utilisateur;
import cardoil.backend.repository.StationRepository;
import cardoil.backend.repository.TransactionRepository;
import cardoil.backend.repository.UtilisateurRepository;
import cardoil.backend.service.AdminCompagnieRapportService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

// Renommé depuis AdminRapportServiceImpl — voir note dans
// AdminCompagnieRapportService.java.
@Service
@RequiredArgsConstructor
public class AdminCompagnieRapportServiceImpl implements AdminCompagnieRapportService {

    private final UtilisateurRepository utilisateurRepository;
    private final TransactionRepository transactionRepository;
    private final StationRepository stationRepository;

    private Compagnie resolveCompagnie(String login) {
        Utilisateur utilisateur = utilisateurRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));
        if (utilisateur.getCompagnie() == null) {
            throw new IllegalStateException("Aucune compagnie associée à cet utilisateur");
        }
        return utilisateur.getCompagnie();
    }

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
    public RapportGlobalResponse getRapport(String login, String periode) {
        Compagnie compagnie = resolveCompagnie(login);
        Long compagnieId = compagnie.getId();

        LocalDateTime debut = resolveDebut(periode);
        LocalDateTime fin = LocalDateTime.now();

        var caTotal = transactionRepository.sumCaByCompagnieAndPeriode(compagnieId, debut, fin);
        long totalTransactions = transactionRepository
                .countByStation_Compagnie_IdAndDateTransactionBetween(compagnieId, debut, fin);
        long transactionsReussies = transactionRepository
                .countByStation_Compagnie_IdAndDateTransactionBetweenAndStatut(
                        compagnieId, debut, fin, StatutTransaction.REUSSIE);
        long transactionsEchec = transactionRepository
                .countByStation_Compagnie_IdAndDateTransactionBetweenAndStatut(
                        compagnieId, debut, fin, StatutTransaction.ECHEC);

        List<Station> stations = stationRepository.findByCompagnieId(compagnieId);
        List<RapportStationResponse> parStation = stations.stream()
                .map(s -> RapportStationResponse.builder()
                        .nom(s.getNom())
                        .ca(transactionRepository.sumCaByStationAndPeriode(s.getId(), debut, fin))
                        .nbTransactions(transactionRepository
                                .countByStationIdAndDateTransactionBetween(s.getId(), debut, fin))
                        .build())
                .toList();

        List<RapportTransactionResponse> dernieresTransactions = transactionRepository
                .findByStation_Compagnie_IdAndDateTransactionBetweenOrderByDateTransactionDesc(
                        compagnieId, debut, fin)
                .stream()
                .limit(10)
                .map(this::toTransactionResponse)
                .toList();

        return RapportGlobalResponse.builder()
                .caTotal(caTotal)
                .totalTransactions(totalTransactions)
                .transactionsReussies(transactionsReussies)
                .transactionsEchec(transactionsEchec)
                .parStation(parStation)
                .dernieresTransactions(dernieresTransactions)
                .build();
    }

    private RapportTransactionResponse toTransactionResponse(Transaction t) {
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