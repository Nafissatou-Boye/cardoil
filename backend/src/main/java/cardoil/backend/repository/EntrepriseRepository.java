package cardoil.backend.repository;

import cardoil.backend.entity.Entreprise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EntrepriseRepository extends JpaRepository<Entreprise, Long> {
    List<Entreprise> findByCompagnieIdOrderByNomAsc(Long compagnieId);
    Optional<Entreprise> findByIdAndCompagnieId(Long id, Long compagnieId);
    boolean existsByCodeAndCompagnieId(String code, Long compagnieId);
}