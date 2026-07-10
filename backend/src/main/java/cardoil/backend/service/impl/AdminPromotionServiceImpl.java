package cardoil.backend.service.impl;

import cardoil.backend.dto.request.PromotionRequest;
import cardoil.backend.dto.response.PromotionResponse;
import cardoil.backend.entity.*;
import cardoil.backend.repository.*;
import cardoil.backend.service.AdminPromotionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminPromotionServiceImpl implements AdminPromotionService {

    private final PromotionRepository promotionRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final StationRepository stationRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final long DUREE_MAX_JOURS = 180;

    private static final Set<StatutPromotion> DEPUIS_DRAFT     = Set.of(StatutPromotion.ACTIVE, StatutPromotion.ARCHIVEE);
    private static final Set<StatutPromotion> DEPUIS_ACTIVE    = Set.of(StatutPromotion.SUSPENDUE, StatutPromotion.ARCHIVEE);
    private static final Set<StatutPromotion> DEPUIS_SUSPENDUE = Set.of(StatutPromotion.ACTIVE, StatutPromotion.ARCHIVEE);

    @Override
    public List<PromotionResponse> getAll(String login) {
        Compagnie compagnie = getCompagnie(login);
        return promotionRepository.findByCompagnieIdOrderByDateDebutDesc(compagnie.getId()).stream()
                .map(this::expirerSiNecessaireEtConvertir)
                .toList();
    }

    @Override
    public PromotionResponse create(String login, PromotionRequest request) {
        Compagnie compagnie = getCompagnie(login);
        validerPeriode(request.getDateDebut(), request.getDateFin());

        List<Station> stations = resolveStations(request.getStationIds(), compagnie.getId());

        Promotion promotion = Promotion.builder()
                .nom(request.getNom())
                .description(request.getDescription())
                .type(request.getType())
                .statut(StatutPromotion.DRAFT)
                .dateDebut(request.getDateDebut())
                .dateFin(request.getDateFin())
                .montantMinimum(request.getMontantMinimum())
                .stationsConcernees(stations)
                .plafondParClient(request.getPlafondParClient())
                .plafondGlobal(request.getPlafondGlobal())
                .plafondJournalier(request.getPlafondJournalier())
                .pointsParTranche(request.getPointsParTranche())
                .montantParTranche(request.getMontantParTranche())
                .descriptionCadeau(request.getDescriptionCadeau())
                .stockCadeaux(request.getStockCadeaux())
                .probabiliteGain(request.getProbabiliteGain())
                .descriptionLot(request.getDescriptionLot())
                .actif(true)
                .compagnie(compagnie)
                .build();

        return toResponse(promotionRepository.save(promotion));
    }

    @Override
    public PromotionResponse update(String login, Long id, PromotionRequest request) {
        Compagnie compagnie = getCompagnie(login);

        Promotion promotion = promotionRepository.findByIdAndCompagnieId(id, compagnie.getId())
                .orElseThrow(() -> new EntityNotFoundException("Promotion non trouvée"));

        if (promotion.getStatut() != StatutPromotion.DRAFT) {
            throw new IllegalStateException("Seule une promotion en brouillon peut être modifiée");
        }

        validerPeriode(request.getDateDebut(), request.getDateFin());

        List<Station> stations = resolveStations(request.getStationIds(), compagnie.getId());

        promotion.setNom(request.getNom());
        promotion.setDescription(request.getDescription());
        promotion.setType(request.getType());
        promotion.setDateDebut(request.getDateDebut());
        promotion.setDateFin(request.getDateFin());
        promotion.setMontantMinimum(request.getMontantMinimum());
        promotion.setStationsConcernees(stations);
        promotion.setPlafondParClient(request.getPlafondParClient());
        promotion.setPlafondGlobal(request.getPlafondGlobal());
        promotion.setPlafondJournalier(request.getPlafondJournalier());
        promotion.setPointsParTranche(request.getPointsParTranche());
        promotion.setMontantParTranche(request.getMontantParTranche());
        promotion.setDescriptionCadeau(request.getDescriptionCadeau());
        promotion.setStockCadeaux(request.getStockCadeaux());
        promotion.setProbabiliteGain(request.getProbabiliteGain());
        promotion.setDescriptionLot(request.getDescriptionLot());

        return toResponse(promotionRepository.save(promotion));
    }

    @Override
    public PromotionResponse changerStatut(String login, Long id, String nouveauStatutStr) {
        Compagnie compagnie = getCompagnie(login);

        Promotion promotion = promotionRepository.findByIdAndCompagnieId(id, compagnie.getId())
                .orElseThrow(() -> new EntityNotFoundException("Promotion non trouvée"));

        StatutPromotion nouveauStatut;
        try {
            nouveauStatut = StatutPromotion.valueOf(nouveauStatutStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Statut invalide");
        }

        StatutPromotion statutActuel = promotion.getStatut();

        boolean transitionValide = switch (statutActuel) {
            case DRAFT     -> DEPUIS_DRAFT.contains(nouveauStatut);
            case ACTIVE    -> DEPUIS_ACTIVE.contains(nouveauStatut);
            case SUSPENDUE -> DEPUIS_SUSPENDUE.contains(nouveauStatut);
            case EXPIREE   -> nouveauStatut == StatutPromotion.ARCHIVEE;
            case ARCHIVEE  -> nouveauStatut == StatutPromotion.DRAFT;
        };

        if (!transitionValide) {
            throw new IllegalStateException("Transition non autorisée : " + statutActuel + " → " + nouveauStatut);
        }

        promotion.setStatut(nouveauStatut);
        return toResponse(promotionRepository.save(promotion));
    }

    @Override
    public void delete(String login, Long id) {
        Compagnie compagnie = getCompagnie(login);

        Promotion promotion = promotionRepository.findByIdAndCompagnieId(id, compagnie.getId())
                .orElseThrow(() -> new EntityNotFoundException("Promotion non trouvée"));

        if (promotion.getStatut() != StatutPromotion.DRAFT) {
            throw new IllegalStateException("Seule une promotion en brouillon peut être supprimée");
        }

        promotionRepository.delete(promotion);
    }

    // ===== HELPERS =====

    private List<Station> resolveStations(List<Long> stationIds, Long compagnieId) {
        if (stationIds == null || stationIds.isEmpty()) return List.of();
        return stationIds.stream()
                .map(sid -> stationRepository.findByIdAndCompagnieId(sid, compagnieId)
                        .orElseThrow(() -> new EntityNotFoundException("Station non trouvée : " + sid)))
                .toList();
    }

    private void validerPeriode(LocalDateTime debut, LocalDateTime fin) {
        if (!fin.isAfter(debut)) {
            throw new IllegalArgumentException("La date de fin doit être après la date de début");
        }
        long jours = Duration.between(debut, fin).toDays();
        if (jours > DUREE_MAX_JOURS) {
            throw new IllegalArgumentException("La durée ne peut pas dépasser " + DUREE_MAX_JOURS + " jours");
        }
    }

    private Promotion expirerSiNecessaire(Promotion promotion) {
        boolean estActiveOuSuspendue = promotion.getStatut() == StatutPromotion.ACTIVE
                || promotion.getStatut() == StatutPromotion.SUSPENDUE;
        if (estActiveOuSuspendue && promotion.getDateFin().isBefore(LocalDateTime.now())) {
            promotion.setStatut(StatutPromotion.EXPIREE);
            promotion = promotionRepository.save(promotion);
        }
        return promotion;
    }

    private PromotionResponse expirerSiNecessaireEtConvertir(Promotion p) {
        return toResponse(expirerSiNecessaire(p));
    }

    private Compagnie getCompagnie(String login) {
        Utilisateur utilisateur = utilisateurRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));
        if (utilisateur.getCompagnie() == null) {
            throw new IllegalStateException("Aucune compagnie associée");
        }
        return utilisateur.getCompagnie();
    }

    private PromotionResponse toResponse(Promotion p) {
        long joursRestants = 0;
        if (p.getStatut() == StatutPromotion.ACTIVE || p.getStatut() == StatutPromotion.SUSPENDUE) {
            joursRestants = Math.max(0, Duration.between(LocalDateTime.now(), p.getDateFin()).toDays());
        }
        long dureeJours = Duration.between(p.getDateDebut(), p.getDateFin()).toDays();

        List<PromotionResponse.StationInfo> stationsInfo = p.getStationsConcernees() == null ? List.of() :
                p.getStationsConcernees().stream()
                        .map(s -> PromotionResponse.StationInfo.builder()
                                .id(s.getId()).nom(s.getNom()).build())
                        .toList();

        return PromotionResponse.builder()
                .id(p.getId())
                .nom(p.getNom())
                .description(p.getDescription())
                .type(p.getType().name())
                .statut(p.getStatut().name())
                .dateDebut(p.getDateDebut().format(FORMATTER))
                .dateFin(p.getDateFin().format(FORMATTER))
                .joursRestants(joursRestants)
                .dureeJours(dureeJours)
                .montantMinimum(p.getMontantMinimum())
                .stationsConcernees(stationsInfo)
                .plafondParClient(p.getPlafondParClient())
                .plafondGlobal(p.getPlafondGlobal())
                .plafondJournalier(p.getPlafondJournalier())
                .pointsParTranche(p.getPointsParTranche())
                .montantParTranche(p.getMontantParTranche())
                .descriptionCadeau(p.getDescriptionCadeau())
                .stockCadeaux(p.getStockCadeaux())
                .probabiliteGain(p.getProbabiliteGain())
                .descriptionLot(p.getDescriptionLot())
                .build();
    }
}