package cardoil.backend.repository;

import cardoil.backend.entity.Promotion;
import cardoil.backend.entity.StatutPromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.transaction.Transactional;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    List<Promotion> findByCompagnieId(Long compagnieId);
    List<Promotion> findByCompagnieIdAndStatut(Long compagnieId, StatutPromotion statut);
    List<Promotion> findByCompagnieIdOrderByDateDebutDesc(Long compagnieId);
Optional<Promotion> findByIdAndCompagnieId(Long id, Long compagnieId);
@Query("DELETE FROM Promotion p WHERE p.statut = cardoil.backend.entity.StatutPromotion.ARCHIVEE " +
       "AND p.dateFin < :limite")
@Modifying
@Transactional
void deleteArchiveesAvant(@Param("limite") LocalDateTime limite);
}