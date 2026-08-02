package cardoil.backend.scheduler;

import cardoil.backend.entity.Carte;
import cardoil.backend.entity.Recharge;
import cardoil.backend.entity.StatutCarte;
import cardoil.backend.entity.TypeRecharge;
import cardoil.backend.repository.CarteRepository;
import cardoil.backend.repository.RechargeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// ═══════════════════════════════════════════════════════════════════════════
// Renouvellement automatique des dotations — CdC §5.1
//
// RECHARGEABLE_LIBRE      : jamais touchée par ce job (recharge libre à tout
//                           moment). En pratique dateRenouvellement devrait
//                           être null pour ce type et la carte ne devrait
//                           donc jamais matcher la requête ci-dessous — le
//                           "continue" reste en garde-fou défensif.
// DOTATION_PLAFONNEE      : solde remis exactement à montantDotationMensuelle
//                           (reliquat perdu, conforme à l'énoncé de l'enum).
// DOTATION_AVEC_REPORT    : solde += montantDotationMensuelle, plafonné à
//                           plafondCumuleMax si défini.
//
// ⚠️ Non fait, à confirmer : le CdC prévoit une alerte à l'Admin Entreprise
// et à Cardoil en cas d'échec. Je logue (log.error) mais je ne déclenche
// aucun email/notification réel — je ne connais pas le canal à utiliser côté
// EmailService pour ce cas précis (différent de envoyerCredentialsEmploye).
// ⚠️ Transaction unique pour tout le batch : une carte en erreur ne fait pas
// échouer les autres (try/catch par carte), mais une erreur inattendue au
// niveau base de données annulerait tout le batch du jour. Dis-moi si tu
// veux isoler chaque carte dans sa propre transaction (demande un second
// bean à cause du self-invocation de Spring @Transactional).
// ═══════════════════════════════════════════════════════════════════════════

@Slf4j
@Component
@RequiredArgsConstructor
public class RenouvellementDotationScheduler {

    private final CarteRepository carteRepository;
    private final RechargeRepository rechargeRepository;

    // Tous les jours à 1h du matin.
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void renouvellerDotationsDuJour() {
        LocalDate aujourdHui = LocalDate.now();
        int jour = aujourdHui.getDayOfMonth();

        // montantDotationMensuelle IS NOT NULL filtré en base : plus besoin
        // de garde défensive dans la boucle pour ce cas (voir switch ci-dessous).
        List<Carte> cartes = carteRepository
                .findByMontantDotationMensuelleIsNotNullAndDateRenouvellementAndStatut(jour, StatutCarte.ACTIVE);
        log.info("Renouvellement dotation — {} carte(s) éligible(s) pour le jour {}", cartes.size(), jour);

        int traitees = 0;
        int ignorees = 0;
        int enErreur = 0;

        for (Carte carte : cartes) {
            try {
                if (aujourdHui.equals(carte.getDerniereDateRenouvellement())) {
                    ignorees++;
                    continue;
                }

                BigDecimal montantDotation = carte.getMontantDotationMensuelle();

                switch (carte.getTypeCarte()) {
                    case RECHARGEABLE_LIBRE -> {
                        // Garde défensive : la requête exclut déjà montantDotationMensuelle
                        // null, donc une carte LIBRE (qui ne devrait jamais avoir ce champ
                        // renseigné) ne devrait jamais atteindre cette branche.
                        ignorees++;
                        continue;
                    }
                    case DOTATION_PLAFONNEE -> carte.setSolde(montantDotation);
                    case DOTATION_AVEC_REPORT -> {
                        BigDecimal nouveauSolde = carte.getSolde().add(montantDotation);
                        if (carte.getPlafondCumuleMax() != null
                                && nouveauSolde.compareTo(carte.getPlafondCumuleMax()) > 0) {
                            nouveauSolde = carte.getPlafondCumuleMax();
                        }
                        carte.setSolde(nouveauSolde);
                    }
                }

                carte.setDerniereDateRenouvellement(aujourdHui);
                carteRepository.save(carte);

                rechargeRepository.save(Recharge.builder()
                        .carte(carte)
                        .montant(montantDotation)
                        .type(TypeRecharge.DOTATION)
                        .effectuePar(null) // automatique — aucun utilisateur humain
                        .build());

                traitees++;
            } catch (Exception e) {
                enErreur++;
                log.error("Renouvellement dotation — échec pour carte {} : {}",
                        carte.getNumeroCarte(), e.getMessage(), e);
                // ⚠️ Alerte Admin Entreprise + Cardoil prévue par le CdC — pas implémentée, voir note en tête de fichier.
            }
        }

        log.info("Renouvellement dotation terminé — {} traitée(s), {} ignorée(s), {} en erreur",
                traitees, ignorees, enErreur);
    }
}