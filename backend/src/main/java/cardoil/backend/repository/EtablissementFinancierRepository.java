package cardoil.backend.repository;

import cardoil.backend.entity.EtablissementFinancier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EtablissementFinancierRepository extends JpaRepository<EtablissementFinancier, Long> {
    Optional<EtablissementFinancier> findByCode(String code);
    boolean existsByCode(String code);
}