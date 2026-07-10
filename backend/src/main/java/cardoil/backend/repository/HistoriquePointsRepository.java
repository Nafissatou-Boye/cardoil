package cardoil.backend.repository;

import cardoil.backend.entity.HistoriquePoints;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoriquePointsRepository extends JpaRepository<HistoriquePoints, Long> {
    List<HistoriquePoints> findByFideliteClientIdOrderByDateOperationDesc(Long fideliteId);
}