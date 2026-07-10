package cardoil.backend.repository;

import cardoil.backend.entity.Cadeau;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CadeauRepository extends JpaRepository<Cadeau, Long> {
    List<Cadeau> findByCompagnieIdOrderByNomAsc(Long compagnieId);
    Optional<Cadeau> findByIdAndCompagnieId(Long id, Long compagnieId);
}