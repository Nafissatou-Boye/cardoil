package cardoil.backend.repository;

import cardoil.backend.entity.PrixJour;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

public interface PrixJourRepository extends JpaRepository<PrixJour, Long> {
    Optional<PrixJour> findByProduitIdAndDatePrix(Long produitId, LocalDate datePrix);
    List<PrixJour> findByProduitIdOrderByDatePrixDesc(Long produitId);
}