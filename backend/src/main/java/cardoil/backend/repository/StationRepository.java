package cardoil.backend.repository;

import cardoil.backend.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface StationRepository extends JpaRepository<Station, Long> {
    List<Station> findByCompagnieId(Long compagnieId);
    List<Station> findByActif(boolean actif);
    boolean existsByGerantId(Long gerantId);
    long countByCompagnieId(Long compagnieId);
long countByCompagnieIdAndActif(Long compagnieId, boolean actif);
Optional<Station> findByIdAndCompagnieId(Long id, Long compagnieId);
List<Station> findByGerantId(Long gerantId);
}