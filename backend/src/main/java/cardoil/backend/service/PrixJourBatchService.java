package cardoil.backend.service;

import cardoil.backend.entity.PrixJour;
import cardoil.backend.entity.PrixProduit;
import cardoil.backend.entity.Produit;
import cardoil.backend.entity.StatutProduit;
import cardoil.backend.repository.PrixJourRepository;
import cardoil.backend.repository.PrixProduitRepository;
import cardoil.backend.repository.ProduitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrixJourBatchService {

    private final ProduitRepository produitRepository;
    private final PrixProduitRepository prixProduitRepository;
    private final PrixJourRepository prixJourRepository;

    // CDC section 10.2 : recommandé entre 00h00 et 04h00. Fixé ici à 1h du matin, heure serveur.
    @Scheduled(cron = "0 0 1 * * *")
    public void alimenterPrixJourDuJour() {
        alimenterPrixJourPourDate(LocalDate.now());
    }

    // CDC section 10.2 : mécanisme de rejeu manuel pour une date passée (ou pour forcer aujourd'hui).
    @Transactional
    public ResultatBatch alimenterPrixJourPourDate(LocalDate date) {
        List<Produit> produitsActifs = produitRepository.findAll().stream()
                .filter(p -> p.getStatut() == StatutProduit.ACTIF)
                .toList();

        int crees = 0;
        int dejaExistants = 0;
        int sansPrixConfigure = 0;

        for (Produit produit : produitsActifs) {
            try {
                boolean existeDeja = prixJourRepository
                        .findByProduitIdAndDatePrix(produit.getId(), date)
                        .isPresent();
                if (existeDeja) {
                    dejaExistants++;
                    continue;
                }

                Optional<PrixProduit> prixEnVigueur = prixProduitRepository.findPrixEnVigueur(produit.getId(), date);

                if (prixEnVigueur.isEmpty()) {
                    sansPrixConfigure++;
                    continue;
                }

                PrixProduit p = prixEnVigueur.get();
                PrixJour prixJour = PrixJour.builder()
                        .produit(produit)
                        .prixTtc(p.getPrixTtc())
                        .prixHtva(p.getPrixHtva())
                        .prixHtt(p.getPrixHtt())
                        .datePrix(date)
                        .build();
                prixJourRepository.save(prixJour);
                crees++;

            } catch (Exception ex) {
                // CDC section 7.2 : "une alerte doit être générée pour l'Admin Compagnie" en cas d'échec.
                // Pas encore branché sur ton système de notification existant (NotificationRepository) —
                // dis-moi si tu veux que je le fasse, je n'ai pas voulu deviner son API.
                log.error("Échec de l'alimentation PrixJour pour le produit {} à la date {}",
                        produit.getId(), date, ex);
            }
        }

        log.info("Batch PrixJour du {} : {} créés, {} déjà existants, {} sans prix configuré",
                date, crees, dejaExistants, sansPrixConfigure);

        return new ResultatBatch(crees, dejaExistants, sansPrixConfigure);
    }

    public record ResultatBatch(int crees, int dejaExistants, int sansPrixConfigure) {}
}