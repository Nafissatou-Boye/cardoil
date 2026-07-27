// ServiceCatalogueRepository.java
package cardoil.backend.repository;

import cardoil.backend.entity.ServiceCatalogue;
import cardoil.backend.entity.StatutService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceCatalogueRepository extends JpaRepository<ServiceCatalogue, Long> {
    List<ServiceCatalogue> findByCompagnieId(Long compagnieId);
    List<ServiceCatalogue> findByCompagnieIdAndStatut(Long compagnieId, StatutService statut);
    Optional<ServiceCatalogue> findByIdAndCompagnieId(Long id, Long compagnieId);
}