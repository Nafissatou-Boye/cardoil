package cardoil.backend.repository;

import cardoil.backend.entity.EtablissementFinancierCompagnie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EtablissementFinancierCompagnieRepository
        extends JpaRepository<EtablissementFinancierCompagnie, Long> {

    Optional<EtablissementFinancierCompagnie> findByEtablissementFinancierIdAndCompagnieId(
            Long etablissementId, Long compagnieId);

    List<EtablissementFinancierCompagnie> findByEtablissementFinancierId(Long etablissementId);

    boolean existsByEtablissementFinancierIdAndCompagnieId(Long etablissementId, Long compagnieId);
}