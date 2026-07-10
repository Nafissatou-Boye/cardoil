package cardoil.backend.service.impl;

import cardoil.backend.entity.*;
import cardoil.backend.repository.FideliteClientRepository;
import cardoil.backend.repository.HistoriquePointsRepository;
import cardoil.backend.service.FideliteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class FideliteServiceImpl implements FideliteService {

    private final FideliteClientRepository fideliteClientRepository;
    private final HistoriquePointsRepository historiquePointsRepository;

    @Override
    public void attribuerPoints(Transaction transaction, Promotion promotion) {

        if (promotion.getType() != TypePromotion.POINTS) return;
        if (promotion.getPointsParTranche() == null || promotion.getMontantParTranche() == null) return;
        if (transaction.getClient() == null) return;

        BigDecimal montant = transaction.getMontant();
        BigDecimal tranche = promotion.getMontantParTranche();

        // Calcul des points : ex. 1 point pour 100 FCFA
        int pointsGagnes = montant.divideToIntegralValue(tranche)
                .multiply(BigDecimal.valueOf(promotion.getPointsParTranche()))
                .intValue();

        if (pointsGagnes <= 0) return;

        // Appliquer plafond journalier si défini
        if (promotion.getPlafondJournalier() != null) {
            pointsGagnes = Math.min(pointsGagnes, promotion.getPlafondJournalier());
        }

        // Récupérer ou créer le solde fidélité du client
        Compagnie compagnie = transaction.getStation().getCompagnie();
        Utilisateur client = transaction.getClient();

        FideliteClient fidelite = fideliteClientRepository
                .findByClientIdAndCompagnieId(client.getId(), compagnie.getId())
                .orElseGet(() -> FideliteClient.builder()
                        .client(client)
                        .compagnie(compagnie)
                        .pointsTotal(0)
                        .pointsDisponibles(0)
                        .pointsUtilises(0)
                        .build());

        // Appliquer plafond par client
        if (promotion.getPlafondParClient() != null) {
            int totalApres = fidelite.getPointsTotal() + pointsGagnes;
            if (totalApres > promotion.getPlafondParClient()) {
                pointsGagnes = Math.max(0, promotion.getPlafondParClient() - fidelite.getPointsTotal());
            }
        }

        if (pointsGagnes <= 0) return;

        fidelite.setPointsTotal(fidelite.getPointsTotal() + pointsGagnes);
        fidelite.setPointsDisponibles(fidelite.getPointsDisponibles() + pointsGagnes);
        fideliteClientRepository.save(fidelite);

        // Enregistrer dans l'historique
        HistoriquePoints historique = HistoriquePoints.builder()
                .fideliteClient(fidelite)
                .type(TypeMouvementPoints.GAIN)
                .points(pointsGagnes)
                .montantTransaction(montant)
                .description("Achat " + (transaction.getProduit() != null
                        ? transaction.getProduit().getNom() : "") + " — Promotion : " + promotion.getNom())
                .transaction(transaction)
                .build();

        historiquePointsRepository.save(historique);
    }
}