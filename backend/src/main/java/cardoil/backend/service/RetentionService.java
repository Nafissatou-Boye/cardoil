package cardoil.backend.service;

import cardoil.backend.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RetentionService {

    private final PromotionRepository promotionRepository;

    // Tourne chaque nuit à 2h00
    @Scheduled(cron = "0 0 2 * * *")
    public void supprimerPromotionsArchivees() {
        LocalDateTime limite = LocalDateTime.now().minusYears(1);
        promotionRepository.deleteArchiveesAvant(limite);
        System.out.println("✅ Nettoyage automatique : promotions archivées depuis plus d'1 an supprimées");
    }
}