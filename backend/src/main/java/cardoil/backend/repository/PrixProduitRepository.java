package cardoil.backend.repository;

import cardoil.backend.entity.PrixProduit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PrixProduitRepository extends JpaRepository<PrixProduit, Long> {

    List<PrixProduit> findByProduitIdOrderByDateDebutDesc(Long produitId);

    // CDC section 7.2, étape 2 : date_debut ≤ date ≤ date_fin OU date_fin IS NULL
    @Query("""
            SELECT p FROM PrixProduit p
            WHERE p.produit.id = :produitId
              AND p.dateDebut <= :date
              AND (p.dateFin IS NULL OR p.dateFin >= :date)
            """)
    Optional<PrixProduit> findPrixEnVigueur(@Param("produitId") Long produitId, @Param("date") LocalDate date);
}