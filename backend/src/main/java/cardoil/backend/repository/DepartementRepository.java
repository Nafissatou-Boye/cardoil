package cardoil.backend.repository;

import cardoil.backend.entity.Departement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartementRepository extends JpaRepository<Departement, Long> {
    List<Departement> findByEntrepriseId(Long entrepriseId);
    Optional<Departement> findByIdAndEntrepriseId(Long id, Long entrepriseId);
}