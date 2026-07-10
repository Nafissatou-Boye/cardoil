package cardoil.backend.repository;

import cardoil.backend.entity.CarteFidelite;
import cardoil.backend.entity.StatutCarte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CarteFideliteRepository extends JpaRepository<CarteFidelite, Long> {
    Optional<CarteFidelite> findByToken(String token);
    List<CarteFidelite> findByUtilisateurId(Long utilisateurId);
    List<CarteFidelite> findByStatut(StatutCarte statut);
}