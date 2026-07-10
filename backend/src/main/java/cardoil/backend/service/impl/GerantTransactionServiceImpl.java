package cardoil.backend.service.impl;

import cardoil.backend.dto.request.TransactionRequest;
import cardoil.backend.dto.response.ProduitResponse;
import cardoil.backend.dto.response.TransactionResponse;
import cardoil.backend.entity.*;
import cardoil.backend.repository.*;
import cardoil.backend.service.FideliteService;
import cardoil.backend.service.GerantTransactionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GerantTransactionServiceImpl implements GerantTransactionService {

    private final UtilisateurRepository utilisateurRepository;
    private final StationRepository stationRepository;
    private final TransactionRepository transactionRepository;
    private final ProduitRepository produitRepository;
    private final PrixJourRepository prixJourRepository;
    private final FideliteService fideliteService;
    private final PromotionRepository promotionRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public TransactionResponse create(String login, TransactionRequest request) {
        Utilisateur gerant = utilisateurRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        Station station = getStation(gerant);

        Produit produitFinal = null;
        BigDecimal prixTtc = null;
        BigDecimal prixHtva = null;
        BigDecimal prixHtt = null;

        if (request.getType() == TypeTransaction.ACHAT) {
            if (request.getProduitId() == null) {
                throw new IllegalArgumentException("Le produit est obligatoire pour un achat");
            }

            Produit p = produitRepository.findByIdAndCompagnieId(
                            request.getProduitId(), station.getCompagnie().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé"));

            if (p.getStatut() != StatutProduit.ACTIF) {
                throw new IllegalStateException("Ce produit n'est pas actif");
            }

            Optional<PrixJour> prixDuJour = prixJourRepository
                    .findByProduitIdAndDatePrix(p.getId(), LocalDate.now());

            PrixJour prix = prixDuJour.orElseGet(() ->
                    prixJourRepository.findByProduitIdOrderByDatePrixDesc(p.getId())
                            .stream().findFirst().orElse(null));

            if (prix != null) {
                prixTtc = prix.getPrixTtc();
                prixHtva = prix.getPrixHtva();
                prixHtt = prix.getPrixHtt();
            }

            produitFinal = p;
        }

        Transaction transaction = Transaction.builder()
                .montant(request.getMontant())
                .type(request.getType())
                .statut(StatutTransaction.REUSSIE)
                .prixTtc(prixTtc)
                .prixHtva(prixHtva)
                .prixHtt(prixHtt)
                .produit(produitFinal)
                .station(station)
                .operateur(gerant)
                .build();

        transaction = transactionRepository.save(transaction);

        // Attribution automatique des points si promotion POINTS active
        final Transaction savedTransaction = transaction;
        final Station savedStation = station;
        promotionRepository.findByCompagnieIdOrderByDateDebutDesc(station.getCompagnie().getId())
                .stream()
                .filter(p -> p.getStatut() == StatutPromotion.ACTIVE
                        && p.getType() == TypePromotion.POINTS
                        && !p.getDateFin().isBefore(LocalDateTime.now())
                        && (p.getStationsConcernees() == null
                            || p.getStationsConcernees().isEmpty()
                            || p.getStationsConcernees().stream()
                                .anyMatch(s -> s.getId().equals(savedStation.getId()))))
                .findFirst()
                .ifPresent(promo -> fideliteService.attribuerPoints(savedTransaction, promo));

        return toResponse(transaction);
    }

    @Override
    public List<TransactionResponse> getRecentes(String login) {
        Utilisateur gerant = utilisateurRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        Station station = getStation(gerant);

        return transactionRepository
                .findTop10ByStationIdOrderByDateTransactionDesc(station.getId())
                .stream().map(this::toResponse).toList();
    }

    @Override
    public List<ProduitResponse> getProduitsDisponibles(String login) {
        Utilisateur gerant = utilisateurRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        Station station = getStation(gerant);

        return produitRepository
                .findByCompagnieIdAndStatut(station.getCompagnie().getId(), StatutProduit.ACTIF)
                .stream().map(this::toProduitResponse).toList();
    }

    private Station getStation(Utilisateur gerant) {
        return stationRepository.findByGerantId(gerant.getId())
                .stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Aucune station assignée à ce gérant"));
    }

    private TransactionResponse toResponse(Transaction t) {
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

    private ProduitResponse toProduitResponse(Produit produit) {
        Optional<PrixJour> dernierPrix = prixJourRepository
                .findByProduitIdOrderByDatePrixDesc(produit.getId())
                .stream().findFirst();

        ProduitResponse.ProduitResponseBuilder builder = ProduitResponse.builder()
                .id(produit.getId())
                .nom(produit.getNom())
                .type(produit.getType().name())
                .statut(produit.getStatut().name())
                .description(produit.getDescription())
                .obligatoire(produit.isObligatoire());

        dernierPrix.ifPresent(p -> builder
                .prixTtcActuel(p.getPrixTtc())
                .prixHtvaActuel(p.getPrixHtva())
                .prixHttActuel(p.getPrixHtt())
                .datePrixActuel(p.getDatePrix().toString()));

        return builder.build();
    }
}