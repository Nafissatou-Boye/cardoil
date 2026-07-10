package cardoil.backend.repository;

import cardoil.backend.entity.Garage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GarageRepository extends JpaRepository<Garage, Long> {
    List<Garage> findByCompagnieId(Long compagnieId);
    List<Garage> findByActif(boolean actif);
}