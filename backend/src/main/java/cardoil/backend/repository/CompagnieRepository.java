package cardoil.backend.repository;

import cardoil.backend.entity.Compagnie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompagnieRepository extends JpaRepository<Compagnie, Long> {
    Optional<Compagnie> findByCode(String code);
    List<Compagnie> findByPaysId(Long paysId);
    List<Compagnie> findByActif(boolean actif);
    boolean existsByCode(String code);
}