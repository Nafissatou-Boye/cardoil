package cardoil.backend.repository;

import cardoil.backend.entity.FideliteClient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FideliteClientRepository extends JpaRepository<FideliteClient, Long> {
    Optional<FideliteClient> findByClientIdAndCompagnieId(Long clientId, Long compagnieId);
}