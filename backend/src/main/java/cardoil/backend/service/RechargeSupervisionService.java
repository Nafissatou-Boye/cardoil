package cardoil.backend.service;

import cardoil.backend.dto.response.RechargeListItemDTO;
import cardoil.backend.dto.response.RechargeStatsDTO;
import cardoil.backend.dto.response.RechargeStatsParPartenaireDTO;
import cardoil.backend.entity.RechargeExterne;
import cardoil.backend.enums.StatutRecharge;
import cardoil.backend.repository.RechargeExterneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RechargeSupervisionService {

    private final RechargeExterneRepository rechargeRepository;

    public Page<RechargeListItemDTO> lister(Long etablissementId, Long compagnieId, StatutRecharge statut,
                                             LocalDateTime dateDebut, LocalDateTime dateFin, Pageable pageable) {
        return rechargeRepository.rechercher(etablissementId, compagnieId, statut, dateDebut, dateFin, pageable)
                .map(this::versListItemDTO);
    }

    public RechargeStatsDTO obtenirStats(Long etablissementId, Long compagnieId,
                                         LocalDateTime dateDebut, LocalDateTime dateFin) {
        List<RechargeExterne> recharges = rechargeRepository.rechercherPourStats(
                etablissementId, compagnieId, dateDebut, dateFin);
        return calculerStats(recharges);
    }

    public List<RechargeStatsParPartenaireDTO> obtenirStatsParPartenaire(LocalDateTime dateDebut, LocalDateTime dateFin) {
        List<RechargeExterne> recharges = rechargeRepository.rechercherPourStats(null, null, dateDebut, dateFin);

        Map<Long, List<RechargeExterne>> parEtablissementId = recharges.stream()
                .collect(Collectors.groupingBy(r -> r.getEtablissementFinancier().getId()));

        return parEtablissementId.entrySet().stream()
                .map(entry -> {
                    List<RechargeExterne> groupe = entry.getValue();
                    RechargeStatsDTO stats = calculerStats(groupe);
                    String nom = groupe.get(0).getEtablissementFinancier().getNom();

                    return RechargeStatsParPartenaireDTO.builder()
                            .etablissementId(entry.getKey())
                            .etablissementNom(nom)
                            .nombreTransactions(stats.getNombreTransactions())
                            .nombreReussies(stats.getNombreReussies())
                            .nombreEchouees(stats.getNombreEchouees())
                            .montantTotalRecharge(stats.getMontantTotalRecharge())
                            .tauxEchec(stats.getTauxEchec())
                            .build();
                })
                .sorted(Comparator.comparingLong(RechargeStatsParPartenaireDTO::getNombreTransactions).reversed())
                .toList();
    }

    private RechargeStatsDTO calculerStats(List<RechargeExterne> recharges) {
        long total = recharges.size();
        long reussies = recharges.stream().filter(r -> r.getStatut() == StatutRecharge.SUCCESS).count();
        long echouees = recharges.stream().filter(r -> r.getStatut() == StatutRecharge.FAILED).count();
        long enAttente = recharges.stream().filter(r -> r.getStatut() == StatutRecharge.PENDING).count();

        BigDecimal montantTotal = recharges.stream()
                .filter(r -> r.getStatut() == StatutRecharge.SUCCESS)
                .map(RechargeExterne::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long termine = reussies + echouees;
        double tauxEchec = termine > 0 ? (echouees * 100.0 / termine) : 0.0;

        List<RechargeExterne> traitees = recharges.stream()
                .filter(r -> r.getDateTraitement() != null)
                .toList();

        Double tempsMoyenMs = traitees.isEmpty() ? null : traitees.stream()
                .mapToLong(r -> Duration.between(r.getDateCreation(), r.getDateTraitement()).toMillis())
                .average()
                .orElse(0.0);

        return RechargeStatsDTO.builder()
                .nombreTransactions(total)
                .nombreReussies(reussies)
                .nombreEchouees(echouees)
                .nombreEnAttente(enAttente)
                .montantTotalRecharge(montantTotal)
                .tauxEchec(tauxEchec)
                .tempsTraitementMoyenMs(tempsMoyenMs)
                .build();
    }

    private RechargeListItemDTO versListItemDTO(RechargeExterne r) {
        return RechargeListItemDTO.builder()
                .id(r.getId())
                .referencePartenaire(r.getReferencePartenaire())
                .etablissementNom(r.getEtablissementFinancier().getNom())
                .compagnieNom(r.getCompagnie().getNom())
                .telephoneClient(r.getTelephoneClient())
                .montant(r.getMontant())
                .devise(r.getDevise())
                .statut(r.getStatut())
                .codeErreur(r.getCodeErreur())
                .dateDemande(r.getDateDemande())
                .dateTraitement(r.getDateTraitement())
                .build();
    }
}