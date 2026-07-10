package cardoil.backend.service.impl;

import cardoil.backend.dto.response.RapportGlobalResponse;
import cardoil.backend.entity.Compagnie;
import cardoil.backend.entity.StatutTransaction;
import cardoil.backend.entity.Utilisateur;
import cardoil.backend.repository.StationRepository;
import cardoil.backend.repository.TransactionRepository;
import cardoil.backend.repository.UtilisateurRepository;
import cardoil.backend.service.AdminRapportService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminRapportServiceImpl implements AdminRapportService {

    private final UtilisateurRepository utilisateurRepository;
    private final StationRepository stationRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public RapportGlobalResponse getRapport(String login, String periode) {
        Compagnie compagnie = getCompagnie(login);
        Long compagnieId = compagnie.getId();

        LocalDateTime fin = LocalDateTime.now();
        LocalDateTime debut = switch (periode) {
            case "7D"  -> fin.minusDays(7);
            case "30D" -> fin.minusDays(30);
            case "3M"  -> fin.minusMonths(3);
            case "12M" -> fin.minusMonths(12);
            default    -> fin.minusDays(30);
        };

        BigDecimal caTotal = transactionRepository
                .sumCaByCompagnieAndPeriode(compagnieId, debut, fin);

        long totalTransactions = transactionRepository
                .countByStation_Compagnie_IdAndDateTransactionBetween(compagnieId, debut, fin);

        long transactionsReussies = transactionRepository
                .countByStation_Compagnie_IdAndDateTransactionBetweenAndStatut(
                        compagnieId, debut, fin, StatutTransaction.REUSSIE);

        long transactionsEchec = transactionRepository
                .countByStation_Compagnie_IdAndDateTransactionBetweenAndStatut(
                        compagnieId, debut, fin, StatutTransaction.ECHEC);

        // Stats par station
        List<RapportGlobalResponse.StationRapport> parStation = stationRepository
                .findByCompagnieId(compagnieId).stream()
                .map(station -> {
                    BigDecimal ca = transactionRepository
                            .sumCaByStationAndPeriode(station.getId(), debut, fin);
                    long nb = transactionRepository
                            .countByStationIdAndDateTransactionBetween(station.getId(), debut, fin);
                    return RapportGlobalResponse.StationRapport.builder()
                            .id(station.getId())
                            .nom(station.getNom())
                            .ca(ca != null ? ca : BigDecimal.ZERO)
                            .nbTransactions(nb)
                            .build();
                })
                .collect(Collectors.toList());

        // Dernières 10 transactions
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        List<RapportGlobalResponse.TransactionRapport> dernieres = transactionRepository
                .findByStation_Compagnie_IdAndDateTransactionBetweenOrderByDateTransactionDesc(
                        compagnieId, debut, fin)
                .stream()
                .limit(10)
                .map(t -> RapportGlobalResponse.TransactionRapport.builder()
                        .id(t.getId())
                        .dateTransaction(t.getDateTransaction().format(formatter))
                        .montant(t.getMontant())
                        .type(t.getType().name())
                        .statut(t.getStatut().name())
                        .station(t.getStation().getNom())
                        .operateur(t.getOperateur().getPrenom() + " " + t.getOperateur().getNom())
                        .build())
                .collect(Collectors.toList());

        return RapportGlobalResponse.builder()
                .caTotal(caTotal != null ? caTotal : BigDecimal.ZERO)
                .totalTransactions(totalTransactions)
                .transactionsReussies(transactionsReussies)
                .transactionsEchec(transactionsEchec)
                .parStation(parStation)
                .dernieresTransactions(dernieres)
                .build();
    }

    private Compagnie getCompagnie(String login) {
        Utilisateur utilisateur = utilisateurRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        if (utilisateur.getCompagnie() == null) {
            throw new IllegalStateException("Aucune compagnie associée à cet utilisateur");
        }

        return utilisateur.getCompagnie();
    }
}