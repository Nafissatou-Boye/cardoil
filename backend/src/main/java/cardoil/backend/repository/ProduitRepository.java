package cardoil.backend.repository;

import cardoil.backend.entity.Produit;
import cardoil.backend.entity.StatutProduit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface ProduitRepository extends JpaRepository<Produit, Long> {
    List<Produit> findByCompagnieId(Long compagnieId);
    List<Produit> findByCompagnieIdAndStatut(Long compagnieId, StatutProduit statut);
Optional<Produit> findByIdAndCompagnieId(Long id, Long compagnieId);
}