package cardoil.backend.repository;

import cardoil.backend.entity.RechargeExterne;
import cardoil.backend.enums.StatutRecharge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RechargeExterneRepository extends JpaRepository<RechargeExterne, UUID> {

    Optional<RechargeExterne> findByEtablissementFinancierIdAndReferencePartenaire(
            Long etablissementId, String referencePartenaire);

    @Query(value = """
            SELECT r FROM RechargeExterne r
            WHERE (:etablissementId IS NULL OR r.etablissementFinancier.id = :etablissementId)
              AND (:compagnieId IS NULL OR r.compagnie.id = :compagnieId)
              AND (:statut IS NULL OR r.statut = :statut)
              AND (:dateDebut IS NULL OR r.dateCreation >= :dateDebut)
              AND (:dateFin IS NULL OR r.dateCreation <= :dateFin)
            ORDER BY r.dateCreation DESC
            """,
            countQuery = """
            SELECT COUNT(r) FROM RechargeExterne r
            WHERE (:etablissementId IS NULL OR r.etablissementFinancier.id = :etablissementId)
              AND (:compagnieId IS NULL OR r.compagnie.id = :compagnieId)
              AND (:statut IS NULL OR r.statut = :statut)
              AND (:dateDebut IS NULL OR r.dateCreation >= :dateDebut)
              AND (:dateFin IS NULL OR r.dateCreation <= :dateFin)
            """)
    Page<RechargeExterne> rechercher(@Param("etablissementId") Long etablissementId,
                                      @Param("compagnieId") Long compagnieId,
                                      @Param("statut") StatutRecharge statut,
                                      @Param("dateDebut") LocalDateTime dateDebut,
                                      @Param("dateFin") LocalDateTime dateFin,
                                      Pageable pageable);

    @Query("""
            SELECT r FROM RechargeExterne r
            WHERE (:etablissementId IS NULL OR r.etablissementFinancier.id = :etablissementId)
              AND (:compagnieId IS NULL OR r.compagnie.id = :compagnieId)
              AND (:dateDebut IS NULL OR r.dateCreation >= :dateDebut)
              AND (:dateFin IS NULL OR r.dateCreation <= :dateFin)
            """)
    List<RechargeExterne> rechercherPourStats(@Param("etablissementId") Long etablissementId,
                                               @Param("compagnieId") Long compagnieId,
                                               @Param("dateDebut") LocalDateTime dateDebut,
                                               @Param("dateFin") LocalDateTime dateFin);
}