package cardoil.backend.service.impl;

import cardoil.backend.dto.response.GerantDashboardResponse;
import cardoil.backend.dto.response.TransactionResponse;
import cardoil.backend.entity.Station;
import cardoil.backend.entity.StatutTransaction;
import cardoil.backend.entity.Utilisateur;
import cardoil.backend.repository.StationRepository;
import cardoil.backend.repository.TransactionRepository;
import cardoil.backend.repository.UtilisateurRepository;
import cardoil.backend.service.GerantService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GerantServiceImpl implements GerantService {

    private final UtilisateurRepository utilisateurRepository;
    private final StationRepository stationRepository;
    private final TransactionRepository transactionRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public GerantDashboardResponse getDashboard(String login) {
        Station station = getStation(login);

        LocalDateTime debutJour = LocalDate.now().atStartOfDay();
        LocalDateTime finJour = LocalDate.now().atTime(23, 59, 59);

        BigDecimal caJour = transactionRepository.sumCaByStationAndPeriode(
                station.getId(), debutJour, finJour);

        long nbTransactionsJour = transactionRepository
                .countByStationIdAndDateTransactionBetween(
                        station.getId(), debutJour, finJour);

        long reussies = transactionRepository
                .countByStationIdAndDateTransactionBetweenAndStatut(
                        station.getId(), debutJour, finJour, StatutTransaction.REUSSIE);

        long echecs = transactionRepository
                .countByStationIdAndDateTransactionBetweenAndStatut(
                        station.getId(), debutJour, finJour, StatutTransaction.ECHEC);

        List<TransactionResponse> dernieres = transactionRepository
                .findTop10ByStationIdOrderByDateTransactionDesc(station.getId())
                .stream()
                .map(this::toResponse)
                .toList();

        return GerantDashboardResponse.builder()
                .stationId(station.getId())
                .stationNom(station.getNom())
                .stationAdresse(station.getAdresse())
                .caJour(caJour)
                .nbTransactionsJour(nbTransactionsJour)
                .transactionsReussiesJour(reussies)
                .transactionsEchecJour(echecs)
                .dernieresTransactions(dernieres)
                .build();
    }

    private Station getStation(String login) {
        Utilisateur utilisateur = utilisateurRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        return stationRepository.findByGerantId(utilisateur.getId())
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Aucune station assignée à ce gérant"));
    }

    private TransactionResponse toResponse(cardoil.backend.entity.Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .dateTransaction(t.getDateTransaction().format(FORMATTER))
                .montant(t.getMontant())
                .type(t.getType().name())
                .statut(t.getStatut().name())
                .produitNom(t.getProduit() != null ? t.getProduit().getNom() : null)
                .prixTtc(t.getPrixTtc())
                .build();
    }
}